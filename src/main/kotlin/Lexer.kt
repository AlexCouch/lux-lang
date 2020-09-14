import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.fix

class Lexer(val input: String){
    private var currentPos = TokenPos.default
    private val scanner = Scanner(input)

    private val Char.delimitingToken: Option<Token> get() = when(this){
        '=' -> Some(Token.EqualToken(currentPos, currentPos))
        '+' -> Some(Token.PlusToken(currentPos, currentPos))
        '-' -> Some(Token.HyphenToken(currentPos, currentPos))
        '*' -> Some(Token.StarToken(currentPos, currentPos))
        '/' -> Some(Token.FSlashToken(currentPos, currentPos))
        ':' -> Some(Token.ColonToken(currentPos, currentPos))
        ',' -> Some(Token.CommaToken(currentPos, currentPos))
        '(' -> Some(Token.LParenToken(currentPos, currentPos))
        ')' -> Some(Token.RParenToken(currentPos, currentPos))
        '[' -> Some(Token.LBracketToken(currentPos, currentPos))
        ']' -> Some(Token.RBracketToken(currentPos, currentPos))
        '{' -> Some(Token.LCurlyToken(currentPos, currentPos))
        '}' -> Some(Token.RCurlyToken(currentPos, currentPos))
        '>' -> Some(Token.RAngleToken(currentPos, currentPos))
        '<' -> Some(Token.LAngleToken(currentPos, currentPos))
        else -> None
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
        while(scanner.current.isDefined()){
            when(val c = scanner.current){
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
                            advance()
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
                        t.delimitingToken != None -> {
                            tokens + (t.delimitingToken as Some).t
                            advance()
                        }
                    }
                }
            }
        }
        return tokens
    }
}