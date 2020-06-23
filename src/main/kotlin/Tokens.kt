sealed class Token(open val pos: TokenPos){
    data class IdentifierToken(val lexeme: String, override val pos: TokenPos): Token(pos)
    data class EqualToken(override val pos: TokenPos): Token(pos)
    data class PlusToken(override val pos: TokenPos): Token(pos)
    data class MinusToken(override val pos: TokenPos): Token(pos)
    data class StarToken(override val pos: TokenPos): Token(pos)
    data class FSlashToken(override val pos: TokenPos): Token(pos)
    data class LParenToken(override val pos: TokenPos): Token(pos)
    data class RParenToken(override val pos: TokenPos): Token(pos)
    data class LBracketToken(override val pos: TokenPos): Token(pos)
    data class RBracketToken(override val pos: TokenPos): Token(pos)
    data class LCurlyToken(override val pos: TokenPos): Token(pos)
    data class RCurlyToken(override val pos: TokenPos): Token(pos)
    data class CommaToken(override val pos: TokenPos): Token(pos)
    data class ColonToken(override val pos: TokenPos): Token(pos)

    data class IntegerLiteralToken(val literal: Int, override val pos: TokenPos): Token(pos)
}

data class TokenPos(val line: Int, val col: Int, val offset: Int, val indentLevel: Int){
    companion object{
        val default = TokenPos(1, 1, 0, 0)
    }
}