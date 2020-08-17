sealed class Token(open val startPos: TokenPos, open val endPos: TokenPos){
    data class IdentifierToken(val lexeme: String, override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class EqualToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class PlusToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class HyphenToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class StarToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class FSlashToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class LParenToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class RParenToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class LBracketToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class RBracketToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class LCurlyToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class RCurlyToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class LAngleToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class RAngleToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class CommaToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class ColonToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class QuoteToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class ApostToken(override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)

    data class IntegerLiteralToken(val literal: Int, override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
    data class StringLiteralToken(val literal: String, override val startPos: TokenPos, override val endPos: TokenPos): Token(startPos, endPos)
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