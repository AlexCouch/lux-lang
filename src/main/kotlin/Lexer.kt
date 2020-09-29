import arrow.core.*
import java.lang.NumberFormatException

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

    fun tokenize(): Option<TokenStream>{
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
                        t.isDigit() && t == '0' && scanner.peek.exists { it == 'x' } -> {
                            val startPos = currentPos
                            val buf = buildString {
                                append(t)
                                scan@ while(true){
                                    val next = advance()
                                    if(next.isEmpty()) break@scan
                                    if(next is Some){
                                        when{
                                            next.t.isLetterOrDigit() -> append(next.t)
                                            else -> break@scan
                                        }
                                    }
                                }
                            }
                            tokens + buf.substringAfter("x").let {
                                when(it.length){
                                    2 -> Token.ByteLiteralToken(it.toByte(16), startPos, currentPos)
                                    4 -> Token.ShortLiteralToken(it.toShort(16), startPos, currentPos)
                                    8 -> Token.IntegerLiteralToken(it.toInt(16), startPos, currentPos)
                                    16 -> Token.LongLiteralToken(it.toLong(16), startPos, currentPos)
                                    else -> {
                                        println("Hex literal too large. Only accepting up to 8 bytes in length, instead found ${it.length}")
                                        return none()
                                    }
                                }
                            }
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
                            val lit = when(val data = buf.toIntOrNull()){
                                null -> when(val d = buf.toLongOrNull()){
                                    null -> {
                                        println("Failed to parse number literal into either integer or long: $buf")
                                        return none()
                                    }
                                    else -> Token.LongLiteralToken(d, startPos, currentPos)
                                }
                                else -> Token.IntegerLiteralToken(data, startPos, currentPos)
                            }
                            tokens + lit
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
        return tokens.some()
    }
}