package errors

import TokenPos
import buildPrettyString

data class SourceOrigin(val start: TokenPos, val end: TokenPos, val source: String){
    override fun toString(): String = buildPrettyString {
        source.lines()
            .drop(start.pos.line)
            .reversed()
            .drop(end.pos.line)
            .forEach {
                appendWithNewLine(it)
            }
    }
    companion object{
        const val SPACING_OFFSET = 2
    }
}

operator fun String.get(range: IntRange) = this.substring(range)