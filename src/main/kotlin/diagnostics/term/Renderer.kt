package diagnostics.term

import PrettyColors
import arrow.core.*
import diagnostics.*
import java.io.IOException
import java.io.Writer
import java.lang.Integer.max
import java.lang.Integer.min

data class Locus(val name: String, val location: Location)

data class SingleLabel(val style: LabelStyle, val range: IntRange, val diagnostic: String)

sealed class MultiLabel{
    object TopLeft: MultiLabel()
    data class Top(val range: IntRange): MultiLabel()
    object Left: MultiLabel()
    data class Bottom(val range: IntRange, val diagnostic: String): MultiLabel()
}

enum class VerticalBound{
    Top,
    Bottom
}

data class Underline(val style: LabelStyle, val verticalBound: VerticalBound)

class Renderer(val writer: Writer, val config: Config){
    val chars get() = config.chars
    val styles get() = config.styles

    fun renderHeader(locus: Option<Locus>, severity: Severity, code: Option<String>, message: String): Option<RenderError>{

        try{
            if(locus is Some){
                when(val result = snippetLocus(locus.t)){
                    is Some -> return result
                    is None -> writer.write(": ")
                }
            }
            when(val result = setColor(styles.header(severity))){
                is Some -> return result
                is None -> {}
            }

            val newcode = code.filter { !it.isEmpty() }
            if(newcode is Some){
                writer.write("[${newcode.t}")
            }
            when(val result = setColor(styles.headerMessage)){
                is Some -> return result
                is None -> {}
            }
            writer.write(": $message")
            when(val result = reset()){
                is Some -> return result
                is None -> {}
            }
        }catch(e: IOException){
            return RenderError.from(Error(e)).some()
        }
        return none()
    }

    fun renderEmpty(): Option<RenderError>{
        try{
            writer.write("\n")
        }catch(e: IOException){
            return RenderError.from(Error(e)).some()
        }
        return none()
    }

    fun renderSnippetStart(outerPadding: Int, locus: Locus): Option<RenderError>{
        try{
            when(val result = outerGutter(outerPadding)){
                is Some -> return result
                is None -> {}
            }
            when(val result = setColor(styles.sourceBorder)){
                is Some -> return result
                is None -> {}
            }
            writer.write("${chars.sourceBorderTopLeft}")
            writer.write("${chars.sourceBorderTop}")
            when(val result = reset()){
                is Some -> return result
                is None -> {}
            }
            writer.write(" \n")
        }catch (e: IOException){
            return RenderError.from(Error(e)).some()
        }
        return none()
    }

    fun renderSnippetSource(
        outerPadding: Int,
        lineNumber: Int,
        source: String,
        severity: Severity,
        singleLabels: Array<SingleLabel>,
        multiLabels: Array<Tuple2<LabelStyle, MultiLabel>>
    ): Option<RenderError>{
        try {
            val src = source.trimEnd { it == '\r' || it == '\n' }
            when (val result = outerGutterNumber(lineNumber, outerPadding)) {
                is Some -> return result
                is None -> {
                }
            }
            when (val result = borderLeft()) {
                is Some -> return result
                is None -> {
                }
            }

            for ((style, label) in multiLabels) {
                when (label) {
                    is MultiLabel.TopLeft ->
                        when (val result = labelMultiTopLeft(severity, style)) {
                            is Some -> return result
                            is None -> {
                            }
                        }
                    is MultiLabel.Top ->
                        when (val result = innerGutterSpace()) {
                            is Some -> return result
                            is None -> {
                            }
                        }
                    is MultiLabel.Left, is MultiLabel.Bottom ->
                        when (val result = labelMultiLeft(severity, style, none())) {
                            is Some -> return result
                            is None -> {
                            }
                        }
                }
            }
            writer.write(" ")
            var inPrimary = false
            for((metrics, ch) in charMetrics(source.toCharArray().withIndex())){
                val colRange = metrics.byteIndex..(metrics.byteIndex + Char.SIZE_BYTES)

                val isPrimary = singleLabels.any {  (style, range, _) ->
                    style == LabelStyle.Primary && isOverlapping(range, colRange)
                } || multiLabels.any { (style, label) ->
                    style == LabelStyle.Primary && when(label){
                        is MultiLabel.Top -> colRange.first >= label.range.last
                        is MultiLabel.TopLeft, MultiLabel.Left -> true
                        is MultiLabel.Bottom -> colRange.last <= label.range.last
                    }
                }

                when{
                    isPrimary && !inPrimary ->{
                        when(val result = setColor(styles.label(severity, LabelStyle.Primary))){
                            is Some -> return result
                            is None -> {}
                        }
                        inPrimary = true
                    }
                    !isPrimary && inPrimary -> {
                        when(val result = reset()){
                            is Some -> return result
                            is None -> {}
                        }
                        inPrimary = false
                    }
                }

                when(ch){
                    '\t' -> (0..metrics.unicodeWidth).forEach { writer.write(" ") }
                    else -> writer.write("$ch")
                }
            }
            if(inPrimary){
                when(val result = reset()){
                    is Some -> return result
                    is None -> {}
                }
            }
            writer.write("\n")

            if(!singleLabels.isEmpty()){
                var messages = 0
                var maxLabelStart = 0
                var maxLabelEnd = 0
                var trailingLabel = none<Tuple2<Int, SingleLabel>>()

                for((labelIndex, label) in singleLabels.withIndex()){
                    val (_, range, message) = label
                    if(!message.isEmpty()){
                        messages += 1
                    }
                    maxLabelStart = max(maxLabelStart, range.first)
                    maxLabelEnd = min(maxLabelEnd, range.last)
                    if(range.last == maxLabelEnd){
                        trailingLabel = if(message.isEmpty()){
                            none()
                        }else{
                            Tuple2(labelIndex, label).some()
                        }
                    }
                }
                if(trailingLabel is Some){
                    val (index, label) = trailingLabel.t
                    val (_, trailingRange, _) = label
                    if(singleLabels.withIndex()
                            .filter { (idx, label) -> idx != index }
                            .any { (_, label) ->
                                val (_, range, _) = label
                                isOverlapping(trailingRange, range)
                            }
                    ){
                        trailingLabel = none()
                    }
                }

                when(val result = outerGutter(outerPadding)){
                    is Some -> return result
                    is None -> {}
                }
                when(val result = borderLeft()){
                    is Some -> return result
                    is None -> {}
                }
                when(val result = innerGutter(severity, multiLabels)){
                    is Some -> return result
                    is None -> {}
                }
                writer.write(" ")

                var prevLabelStyle = none<LabelStyle>()
                val placeholderMetrics = Metrics(source.length, 1)
                for((metrics, ch) in charMetrics(source.toCharArray().withIndex())){
                    val colRange = metrics.byteIndex..(metrics.byteIndex + Char.SIZE_BYTES)
                    val currentLabelStyle = singleLabels.filter { (_, range, _) -> isOverlapping(range, colRange) }
                        .map { (style, _, _) -> style }
                        .maxBy { when(it){
                            LabelStyle.Primary -> 0
                            LabelStyle.Secondary -> 1
                        } }.toOption()
                    if(prevLabelStyle != currentLabelStyle){
                        when(currentLabelStyle){
                            is Some -> when(val result = setColor(styles.label(severity, currentLabelStyle.t))){
                                is Some -> return result
                                is None -> {}
                            }
                            is None -> when(val result = reset()){
                                is Some -> return result
                                is None -> {}
                            }
                        }
                    }

                    val caret = when(currentLabelStyle){
                        is Some -> when(currentLabelStyle.t){
                            LabelStyle.Primary -> chars.singlePrimaryCaret.some()
                            LabelStyle.Secondary -> chars.singleSecondaryCaret.some()
                        }
                        is None -> none()
                    }
                    if(caret is Some){
                        val caretCh = caret.t
                        (0..metrics.unicodeWidth).forEach {
                            writer.write("$caretCh")
                        }
                    }
                    prevLabelStyle = currentLabelStyle
                }
                if(prevLabelStyle is Some){
                    when(val result = reset()){
                        is Some -> return result
                        is None -> {}
                    }
                }
                if(trailingLabel is Some){
                    val (idx, label) = trailingLabel.t
                    val (style, _, message) = label
                    writer.write(" ")
                    when(val result = setColor(styles.label(severity, style))){
                        is Some -> return result
                        is None -> {}
                    }
                    writer.write(message)
                    when(val result = reset()){
                        is Some -> return result
                        is None -> {}
                    }
                    writer.write("\n")

                    if(messages > trailingLabel.t.a){
                        when(val result = outerGutter(outerPadding)){
                            is Some -> return result
                            is None -> {}
                        }
                        when(val result = borderLeft()){
                            is Some -> return result
                            is None -> {}
                        }
                        when(val result = innerGutter(severity, multiLabels)){
                            is Some -> return result
                            is None -> {}
                        }
                        writer.write(" ")
                        when(val result = caretPointers(severity, maxLabelStart, singleLabels, trailingLabel, source.toCharArray().withIndex().toList())){
                            is Some -> return result
                            is None -> {}
                        }
                        writer.write("\n")
                        for((style, range, message) in hangingLabels(singleLabels, trailingLabel).reversed()){
                            TODO()
                        }
                    }
                }
            }
        }catch(e: IOException){
            return RenderError.from(Error(e)).some()
        }
        return none()
    }

    private fun outerGutterNumber(lineNumber: Int, outerPadding: Int): Option<RenderError> {
        when(val result = setColor(styles.lineNumber)){
            is Some -> return result
            is None -> {}
        }
        writer.write("$lineNumber")
        for(i in 0..outerPadding){
            writer.write(" ")
        }
        writer.write(" ")
        return none()
    }

    fun innerGutter(
        severity: Severity,
        multiLabels: Array<Tuple2<LabelStyle, MultiLabel>>
    ): Option<RenderError>{
        try{
            for((idx, label) in multiLabels.withIndex()){
                val (style, lab) = label
                when (lab) {
                    is MultiLabel.TopLeft, is MultiLabel.Left, is MultiLabel.Bottom ->{
                        when(val result = labelMultiLeft(severity, style, none())){
                            is Some -> return result
                            is None -> {}
                        }
                    }
                    is MultiLabel.Top -> {
                        when(val result = innerGutterSpace()){
                            is Some -> return result
                            is None -> {}
                        }
                    }
                }
            }
        }catch(e: IOException){
            return RenderError.from(Error(e)).some()
        }
        return none()
    }

    fun labelMultiLeft(severity: Severity, style: LabelStyle, underline: Option<Underline>): Option<RenderError>{
        when(underline){
            is Some -> {
                when(val result = setColor(styles.label(severity, style))){
                    is Some -> return result
                    is None -> {}
                }
                writer.write("${chars.multiTop}")
                when(val result = reset()){
                    is Some -> return result
                    is None -> {}
                }
            }
            is None -> writer.write(" ")
        }
        when(val result = setColor(styles.label(severity, style))){
            is Some -> return result
            is None -> {}
        }
        writer.write("${chars.multiLeft}")
        when(val result = reset()){
            is Some -> return result
            is None -> {}
        }
        return none()
    }

    fun labelMultiTopLeft(
        severity: Severity,
        style: LabelStyle
    ): Option<RenderError>{
        writer.write(" ")
        when(val result = setColor(styles.label(severity, style))){
            is Some -> return result
            is None -> {}
        }
        writer.write("${chars.multiTopLeft}")
        when(val result = reset()){
            is Some -> return result
            is None -> {}
        }
        return none()
    }

    fun labelMultiBottomLeft(
        severity: Severity,
        style: LabelStyle
    ): Option<RenderError>{
        writer.write(" ")
        when(val result = setColor(styles.label(severity, style))){
            is Some -> return result
            is None -> {}
        }
        writer.write("${chars.multiBottomLeft}")
        when(val result = reset()){
            is Some -> return result
            is None -> {}
        }
        return none()
    }

    fun labelMultiTopCaret(
        severity: Severity,
        style: LabelStyle,
        source: String,
        range: IntRange
    ): Option<RenderError>{
        when(val result = setColor(styles.label(severity, style))){
            is Some -> return result
            is None -> {}
        }
        for((metrics, _) in charMetrics(
            source.toCharArray()
                .withIndex())
            .takeWhile { (metrics, _) -> metrics.byteIndex < range.last + 1 }
        ){
            (0..metrics.unicodeWidth)
                .forEach { writer.write("${chars.multiTop}") }
        }
        val caretStart = when(style){
            LabelStyle.Primary -> config.chars.multiPrimaryCaretStart
            LabelStyle.Secondary -> config.chars.multiSecondarCaretStart
        }
        writer.write("$caretStart")
        when(val result = reset()){
            is Some -> return result
            is None -> {}
        }
        writer.write("\n")
        return none()
    }

    fun labelMultiBottomCaret(
        severity: Severity,
        style: LabelStyle,
        source: String,
        range: IntRange
    ): Option<RenderError>{
        when(val result = setColor(styles.label(severity, style))){
            is Some -> return result
            is None -> {}
        }
        for((metrics, _) in charMetrics(
            source.toCharArray()
                .withIndex())
            .takeWhile { (metrics, _) -> metrics.byteIndex < range.last + 1 }
        ){
            (0..metrics.unicodeWidth)
                .forEach { writer.write("${chars.multiBottom}") }
        }
        val caretStart = when(style){
            LabelStyle.Primary -> config.chars.multiPrimaryCaretStart
            LabelStyle.Secondary -> config.chars.multiSecondarCaretStart
        }
        writer.write("$caretStart")
        when(val result = reset()){
            is Some -> return result
            is None -> {}
        }
        writer.write("\n")
        return none()
    }

    fun innerGutterSpace(): Option<RenderError>{
        try{
            writer.write("  ")
        }catch(e: IOException){
            return RenderError.from(Error(e)).some()
        }
        return none()
    }

    fun borderLeft(): Option<RenderError>{
        try{
            when(val result = setColor(styles.sourceBorder)){
                is Some -> return result
                is None -> {}
            }
            writer.write("${chars.sourceBorderLeft}")
            when(val result = reset()){
                is Some -> return result
                is None -> {}
            }
        }catch (e: IOException){
            return RenderError.from(Error(e)).some()
        }
        return none()

    }

    fun borderLeftBreak(): Option<RenderError>{
        try{
            when(val result = setColor(styles.sourceBorder)){
                is Some -> return result
                is None -> {}
            }
            writer.write("${chars.sourceBorderLeftBreak}")
            when(val result = reset()){
                is Some -> return result
                is None -> {}
            }
        }catch (e: IOException){
            return RenderError.from(Error(e)).some()
        }
        return none()

    }

    fun caretPointers(
        severity: Severity,
        maxLabelStart: Int,
        singleLabels: Array<SingleLabel>,
        trailingLabel: Option<Tuple2<Int, SingleLabel>>,
        charIndicies: List<IndexedValue<Char>>
    ): Option<RenderError>{
        try {
            for ((metric, ch) in charMetrics(charIndicies)) {
                val colRange = metric.byteIndex..(metric.byteIndex + Char.SIZE_BYTES)
                val labelStyle = hangingLabels(singleLabels, trailingLabel)
                    .filter { (_, range, _) -> colRange.contains(range.first) }
                    .map { (style, _, _) -> style }
                    .maxBy {
                        when (it) {
                            LabelStyle.Primary -> 0
                            LabelStyle.Secondary -> 1
                        }
                    }.toOption()
                val spaces = when (labelStyle) {
                    is Some -> {
                        when (val result = setColor(styles.label(severity, labelStyle.t))) {
                            is Some -> return result
                            is None -> {
                            }
                        }
                        writer.write("${chars.pointerLeft}")
                        when (val result = reset()) {
                            is Some -> return result
                            is None -> {
                            }
                        }
                        1..metric.unicodeWidth
                    }
                    is None -> 0..metric.unicodeWidth
                }
                if (metric.byteIndex <= maxLabelStart) {
                    spaces.forEach { writer.write(" ") }
                }
            }
        }catch(e: IOException){
            return RenderError.from(Error(e)).some()
        }
        return none()
    }

    fun hangingLabels(
        singleLabels: Array<SingleLabel>,
        trailingLabel: Option<Tuple2<Int, SingleLabel>>
    ): List<SingleLabel> =
        singleLabels.withIndex()
            .filter { (idx, label) ->
                val (_, _, message) = label
                !message.isEmpty()
            }.filter { (idx, label) ->
                trailingLabel.exists { it.a != idx }
            }.map { (_, label) ->
                label
            }

    fun isOverlapping(range0: IntRange, range1: IntRange): Boolean{
        val start = max(range0.first, range1.first)
        val end = min(range0.last, range1.last)
        return start < end
    }

    fun charMetrics(chars: Iterable<IndexedValue<Char>>): Iterable<Tuple2<Metrics, Char>>{
        val tabWidth = config.tabWidth
        var unicodeColumn = 0

        return chars.map { (idx, c) ->
            val metrics = Metrics(
                idx,
                when{
                c == '\t' && tabWidth == 0 -> 0
                c == '\t' -> tabWidth - (unicodeColumn % tabWidth)
                else -> Char.SIZE_BYTES
            })
            unicodeColumn += metrics.unicodeWidth
            Tuple2(metrics, c)
        }
    }

    fun outerGutter(outerPadding: Int): Option<RenderError>{
        try{
            for(i in 0..outerPadding){
                writer.write(" ")
            }
        }catch(e: IOException){
            return RenderError.from(Error(e)).some()
        }
        return none()
    }

    fun reset(): Option<RenderError>{
        try{
            writer.write(PrettyColors.RESET.ansi)
        }catch(e: IOException){
            return RenderError.from(Error(e)).some()
        }
        return none()
    }

    fun setColor(color: PrettyColors): Option<RenderError>{
        try{
            writer.write(color.ansi)
        }catch(e: IOException){
            return RenderError.from(Error(e)).some()
        }
        return none()
    }

    fun snippetLocus(locus: Locus): Option<RenderError>{
        try{
            writer.write("${locus.name}:${locus.location.line}:${locus.location.column}")
        }catch(e: IOException){
            return RenderError.from(Error(e)).some()
        }
        return none()
    }
}

data class Metrics(val byteIndex: Int, val unicodeWidth: Int)