class Lexer(input: String){
    private var currentPos = TokenPos.default
    private val scanner = Scanner(input)

    private val Char.delimitingToken: Either<Token> get() = when(this){
        '=' -> Either.Some(Token.EqualToken(currentPos))
        '+' -> Either.Some(Token.PlusToken(currentPos))
        '-' -> Either.Some(Token.HyphenToken(currentPos))
        '*' -> Either.Some(Token.StarToken(currentPos))
        '/' -> Either.Some(Token.FSlashToken(currentPos))
        ':' -> Either.Some(Token.ColonToken(currentPos))
        ',' -> Either.Some(Token.CommaToken(currentPos))
        '(' -> Either.Some(Token.LParenToken(currentPos))
        ')' -> Either.Some(Token.RParenToken(currentPos))
        '[' -> Either.Some(Token.LBracketToken(currentPos))
        ']' -> Either.Some(Token.RBracketToken(currentPos))
        '{' -> Either.Some(Token.LCurlyToken(currentPos))
        '}' -> Either.Some(Token.RCurlyToken(currentPos))
        '>' -> Either.Some(Token.RAngleToken(currentPos))
        '<' -> Either.Some(Token.LAngleToken(currentPos))
        else -> Either.None
    }

    fun advance(): Char?{
        val next = scanner.next()
        currentPos = TokenPos(currentPos.line, currentPos.col + 1, scanner.idx, currentPos.indentLevel)
        return next
    }

    fun nextLine(): Char?{
        val next = scanner.next()
        currentPos = TokenPos(currentPos.line + 1, 0, scanner.idx, 0)
        return next
    }

    fun nextTab(){
        currentPos = TokenPos(currentPos.line, currentPos.col + 1, scanner.idx, currentPos.indentLevel + 1)
    }

    fun tokenize(): TokenStream{
        val tokens = TokenStream()
        while(scanner.hasNext() || scanner.current != null){
            val c = scanner.current
            when{
                c?.isWhitespace() == true -> when(c) {
                    '\r' -> if(scanner.next() == '\n'){
                        nextLine()
                    }else{
                        nextLine()
                    }
                    '\n' -> nextLine()
                    '\t' -> nextTab()
                    else -> {
                        val buf = buildString {
                            while(scanner.current?.isWhitespace() == true){
                                append(scanner.current!!)
                                advance()
                            }
                        }
                        if(buf == "    "){
                            nextTab()
                        }
                    }
                }
                c?.isLetter() == true -> {
                    val startPos = currentPos
                    val buf = buildString {
                        append(c)
                        scan@ while(true){
                            val next = advance() ?: break@scan
                            when{
                                next.isLetterOrDigit() -> append(next)
                                next == '_' -> append(next)
                                else -> break@scan
                            }
                        }
                    }
                    tokens + Token.IdentifierToken(buf, startPos)
                }
                c?.isDigit() == true -> {
                    val startPos = currentPos
                    val buf = buildString {
                        append(c)
                        scan@ while(true){
                            val next = advance() ?: break@scan
                            when{
                                next.isDigit() -> append(next)
                                else -> break@scan
                            }
                        }
                    }
                    tokens + Token.IntegerLiteralToken(buf.toInt(), startPos)
                }
                c?.delimitingToken != Either.None -> {
                    tokens + c!!.delimitingToken.unwrap() as Token
                    advance()
                }
            }
        }
        return tokens
    }
}