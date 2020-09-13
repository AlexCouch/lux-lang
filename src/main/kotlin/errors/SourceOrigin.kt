package errors

import TokenPos
import buildPrettyString

data class SourceOrigin(val start: TokenPos, val end: TokenPos, val source: String){
    @ExperimentalStdlibApi
    override fun toString(): String = buildPrettyString {
        source.lines()
            .drop(start.pos.line)
            .filter {
                it.isNotBlank()
            }
            .take(if(end.pos.line - start.pos.line <= 0){
                    1
                }else{
                end.pos.line - start.pos.line
            })
            .forEach {
                appendWithNewLine(it)
            }
    }
    companion object{
        const val SPACING_OFFSET = 2
    }
}

operator fun String.get(range: IntRange) = this.substring(range)