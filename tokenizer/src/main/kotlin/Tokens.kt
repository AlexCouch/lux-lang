sealed class Token(open val startPos: TokenPos, open val endPos: TokenPos){
    data class IdentifierToken(val lexeme: String, override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append(lexeme)
            }
    }
    data class DotToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('.')
            }
    }
    data class EqualToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('=')
            }
    }
    data class SemicolonToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append(';')
            }
    }
    data class PlusToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('+')
            }
    }
    data class HyphenToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('-')
            }
    }
    data class StarToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('*')
            }
    }
    data class FSlashToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('/')
            }
    }
    data class LParenToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('(')
            }
    }
    data class RParenToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append(')')
            }
    }
    data class LBracketToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('[')
            }
    }
    data class RBracketToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append(']')
            }
    }
    data class LCurlyToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('{')
            }
    }
    data class RCurlyToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('}')
            }
    }
    data class LAngleToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('<')
            }
    }
    data class RAngleToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('>')
            }
    }
    data class CommaToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append(',')
            }
    }
    data class ColonToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append(':')
            }
    }
    data class QuoteToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('"')
            }
    }
    data class ApostToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append('\'')
            }
    }

    @ExperimentalUnsignedTypes
    data class ByteLiteralToken(val literal: UByte, override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append(literal)
            }
    }
    data class ShortLiteralToken(val literal: Short, override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append(literal)
            }
    }
    data class IntegerLiteralToken(val literal: Int, override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append(literal)
            }
    }
    data class LongLiteralToken(val literal: Long, override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append(literal)
            }
    }
    data class StringLiteralToken(val literal: String, override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append(literal)
            }
    }
}

data class Position(val line: Int, val col: Int){
    companion object{
        val default = Position(0, 0)
    }
}
data class TokenPos(val pos: Position, val offset: Int, val indentLevel: Int){
    companion object{
        val default = TokenPos(Position.default, 1, 0)
    }
}