package errors

import buildPrettyString

data class SourceAnnotation(val message: String, val line: ErrorLine, val sourceOrigin: SourceOrigin){
    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            append("${line.start.pos.line + 1}:${line.start.pos.col + 1}")
            val lineNumSpace = when{
                sourceOrigin.start.pos.line >= 10       -> SourceOrigin.SPACING_OFFSET - 1
                sourceOrigin.start.pos.line >= 100      -> SourceOrigin.SPACING_OFFSET - 2
                sourceOrigin.start.pos.line >= 1000     -> SourceOrigin.SPACING_OFFSET - 3
                sourceOrigin.start.pos.line >= 10000    -> SourceOrigin.SPACING_OFFSET - 4
                else                                -> SourceOrigin.SPACING_OFFSET
            }
            val colNumSpace = when{
                sourceOrigin.start.pos.col >= 10       -> SourceOrigin.SPACING_OFFSET - 1
                sourceOrigin.start.pos.col >= 100      -> SourceOrigin.SPACING_OFFSET - 2
                sourceOrigin.start.pos.col >= 1000     -> SourceOrigin.SPACING_OFFSET - 3
                sourceOrigin.start.pos.col >= 10000    -> SourceOrigin.SPACING_OFFSET - 4
                else                                -> SourceOrigin.SPACING_OFFSET
            }
            spaced(lineNumSpace)
            append("|")
            spaced(SourceOrigin.SPACING_OFFSET)
//            spaced(spacing + 1 + SourceOrigin.SPACING_OFFSET){
//            }
            red {
                indentN(sourceOrigin.start.indentLevel){
                    append(sourceOrigin.toString())
                }
                spaced(
                    (sourceOrigin.start.pos.line.toString().length +
                            sourceOrigin.start.pos.col.toString().length +
                            lineNumSpace +
                            + PrettyColors.RED.ansi.length
                        ) +
                            line.start.pos.col
                ) {
                    indentN(sourceOrigin.start.indentLevel) {
                        '~' padded (line.end.offset .. line.start.offset)
                    }
                }
                appendWithNewLine(" $message")
            }
        }
}

fun buildSourceAnnotation(block: SourceAnnotationBuilder.()->Unit): SourceAnnotation{
    val sourceAnnotationBuilder = SourceAnnotationBuilder()
    sourceAnnotationBuilder.block()
    return sourceAnnotationBuilder.build()
}