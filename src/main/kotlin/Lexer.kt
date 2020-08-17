import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.fix

class Lexer(val input: String){
    private var currentPos = TokenPos.default
    private val scanner = Scanner(input)

    private val Char.delimitingToken: Either<Token> get() = when(this){
        '=' -> Either.Some(Token.EqualToken(currentPos, currentPos))
        '+' -> Either.Some(Token.PlusToken(currentPos, currentPos))
        '-' -> Either.Some(Token.HyphenToken(currentPos, currentPos))
        '*' -> Either.Some(Token.StarToken(currentPos, currentPos))
        '/' -> Either.Some(Token.FSlashToken(currentPos, currentPos))
        ':' -> Either.Some(Token.ColonToken(currentPos, currentPos))
        ',' -> Either.Some(Token.CommaToken(currentPos, currentPos))
        '(' -> Either.Some(Token.LParenToken(currentPos, currentPos))
        ')' -> Either.Some(Token.RParenToken(currentPos, currentPos))
        '[' -> Either.Some(Token.LBracketToken(currentPos, currentPos))
        ']' -> Either.Some(Token.RBracketToken(currentPos, currentPos))
        '{' -> Either.Some(Token.LCurlyToken(currentPos, currentPos))
        '}' -> Either.Some(Token.RCurlyToken(currentPos, currentPos))
        '>' -> Either.Some(Token.RAngleToken(currentPos, currentPos))
        '<' -> Either.Some(Token.LAngleToken(currentPos, currentPos))
        else -> Either.None
    }

    fun advance(): Option<Char> {
        val next = scanner.next()
        currentPos = TokenPos(Position(currentPos.pos.line, currentPos.pos.col + 1), scanner.idx, currentPos.indentLevel)
        return next
    }

    fun nextLine(): Option<Char>{
        val next = scanner.next()
        currentPos = TokenPos(Position(currentPos.pos.line + 1, 0), scanner.idx, 0)
        return next
    }

    fun nextTab(): Option<Char>{
        val next = scanner.next()
        currentPos = TokenPos(Position(currentPos.pos.line, currentPos.pos.col + 1), scanner.idx, currentPos.indentLevel + 1)
        return next
    }

    fun tokenize(): TokenStream{
        val tokens = TokenStream(input)
        while(scanner.hasNext() || scanner.current.isDefined()){
            val c = scanner.current
            when(c){
                is Some -> {
                    val t = c.t
                    when{
                        t.isWhitespace() -> when(t) {
                            '\r' -> when(val next = scanner.next()) {
                                is Some -> {
                                    if (next.t == '\n') {
                                        nextLine()
                                    } else {
                                        nextLine()
                                    }
                                }
                            }
                            '\n' -> nextLine()
                            '\t' -> nextTab()
                            else -> {
                                val buf = buildString {
                                    while(scanner.current is Some<Char> && (scanner.current as Some<Char>).t.isWhitespace()){
                                        append((scanner.current as Some).t)
                                        advance()
                                    }
                                }
                                if(buf == "    "){
                                    nextTab()
                                }
                            }
                        }
                        t.isLetter() -> {
                            val startPos = currentPos
                            val buf = buildString {
                                append(t)
                                scan@ while(true){
                                    val next = advance()
                                    if(next.isEmpty()) break@scan
                                    if(next is Some){
                                        when{
                                            next.t.isLetterOrDigit() -> append(next.t)
                                            next.t == '_' -> append(next.t)
                                            else -> break@scan
                                        }
                                    }
                                }
                            }
                            tokens + Token.IdentifierToken(buf, startPos, currentPos)
                        }
                        t.isDigit() -> {
                            val startPos = currentPos
                            val buf = buildString {
                                append(t)
                                scan@ while(true){
                                    val next = advance()
                                    if(next.isEmpty()) break@scan
                                    if(next is Some){
                                        when{
                                            next.t.isDigit() -> append(next.t)
                                            else -> break@scan
                                        }
                                    }
                                }
                            }
                            tokens + Token.IntegerLiteralToken(buf.toInt(), startPos, currentPos)
                        }
                        t == '"' -> {
                            val startPos = currentPos
                            val buf = buildString {
                                scan@ while(true){
                                    val next = advance()
                                    if(next.isEmpty()) break@scan
                                    if(next is Some){
                                        when (next.t) {
                                            '"' -> break@scan
                                            else -> append(next.t)
                                        }
                                    }
                                }
                            }
                            tokens + Token.StringLiteralToken(buf, startPos, currentPos)
                        }
                        t == '\'' -> {
                            val startPos = currentPos
                            val buf = buildString {
                                scan@ while(true){
                                    val next = advance()
                                    if(next.isEmpty()) break@scan
                                    if(next is Some){
                                        when (next.t) {
                                            '\'' -> break@scan
                                            else -> append(next.t)
                                        }
                                    }
                                }
                            }
                            tokens + Token.StringLiteralToken(buf, startPos, currentPos)
                        }
                        t.delimitingToken != Either.None -> {
                            tokens + t.delimitingToken.unwrap()
                            advance()
                        }
                    }
                }
            }
        }
        return tokens
    }
}