import arrow.core.*
import java.io.File
import kotlin.experimental.and

val String.isSizeModifier: Boolean get() = this in arrayOf("BYTE", "WORD", "DWORD", "QWORD")

class ASMLoader(private val file: File){
    private val lexer = Lexer(file.readText())

    /**
     * We cache the executable itself to save on time in case [getExecutable] is ran again for some reason
     */
    val executable: Option<Executable> = none()
        get(){
            if(field is Some){
                return field
            }
            return when(val result = parseFile()){
                is Either.Left -> result.a.some()
                is Either.Right -> {
                    println(result.b)
                    none()
                }
            }
        }

    private var bytes = arrayOf<UByte>()

    /**
     * A hashmap of labels where the key is the label name and the value is the index in memory
     *
     * A label is a place in the assembly code that may be referenced multiple times. It is a shorthand notation
     * for pointing to a place in code, which may be used for referencing static data (variables in data section)
     * or for jumping to, or for use with a calling convention.
     *
     * Example:
     *  ```
     *  some_label:
     *      MOV     0, 10 ;Move the integer 10 into 0
     *
     *  start:
     *      JMP some_label ;Jump to the label `some_label` and start executing from there
     *  ```
     */
    private val labels = hashMapOf<String, Int>()

    /*
        TODO: Implement labels so that every time we process a label, we save it's place in the executable,
         so that we can use it again later
     */
    private fun parseRef(tokens: TokenStream): Either<UByteArray, String>{
        val next = when(val next = tokens.next()){
            is None -> return "Expected an operand after REF instruction but instead found EOF".right()
            is Some -> next.t
        }
        var bytes = ubyteArrayOf(InstructionSet.REF.code)
        when(next){
            is Token.ByteLiteralToken, is Token.ShortLiteralToken, is Token.IntegerLiteralToken, is Token.LongLiteralToken ->
                bytes += when(val result = parseIntegerToBytes(next)){
                    is Either.Left -> result.a
                    is Either.Right -> return result
                }
            is Token.IdentifierToken -> when(next.lexeme.toUpperCase()){
                "BYTE" -> {
                    bytes += InstructionSet.BYTE.code
                    bytes += when(val n = tokens.next()){
                        is Some -> when(n.t){
                            is Token.ByteLiteralToken, is Token.ShortLiteralToken, is Token.IntegerLiteralToken, is Token.LongLiteralToken ->
                                when(val result = parseIntegerToBytes(n.t)){
                                    is Either.Left -> result.a
                                    is Either.Right -> return result
                                }
                            else -> return "Invalid opcode: ${n.t}".right()
                        }
                        is None -> return "Expected an operand after BYTE instruction but instead found EOF".right()
                    }
                }
                "WORD" -> {
                    bytes += InstructionSet.WORD.code
                    bytes += when(val n = tokens.next()){
                        is Some -> when(n.t){
                            is Token.ByteLiteralToken, is Token.ShortLiteralToken, is Token.IntegerLiteralToken, is Token.LongLiteralToken ->
                                when(val result = parseIntegerToBytes(n.t)){
                                    is Either.Left -> result.a
                                    is Either.Right -> return result
                                }
                            else -> return "Invalid opcode: ${n.t}".right()
                        }
                        is None -> return "Expected an operand after BYTE instruction but instead found EOF".right()
                    }
                }
                "DWORD" -> {
                    bytes += InstructionSet.DWORD.code
                    bytes += when(val n = tokens.next()){
                        is Some -> when(n.t){
                            is Token.ByteLiteralToken, is Token.ShortLiteralToken, is Token.IntegerLiteralToken, is Token.LongLiteralToken ->
                                when(val result = parseIntegerToBytes(n.t)){
                                    is Either.Left -> result.a
                                    is Either.Right -> return result
                                }
                            else -> return "Invalid opcode: ${n.t}".right()
                        }
                        is None -> return "Expected an operand after BYTE instruction but instead found EOF".right()
                    }
                }
                "QWORD" -> {
                    bytes += InstructionSet.QWORD.code
                    bytes += when(val n = tokens.next()){
                        is Some -> when(n.t){
                            is Token.ByteLiteralToken, is Token.ShortLiteralToken, is Token.IntegerLiteralToken, is Token.LongLiteralToken ->
                                when(val result = parseIntegerToBytes(n.t)){
                                    is Either.Left -> result.a
                                    is Either.Right -> return result
                                }
                            else -> return "Invalid opcode: ${n.t}".right()
                        }
                        is None -> return "Expected an operand after BYTE instruction but instead found EOF".right()
                    }
                }
            }
            else -> return "Expected a memory address to reference, found $next".right()
        }
        when(val rbracket = tokens.next()){
            is Some -> when(rbracket.t){
                is Token.RBracketToken -> {}
                else -> return "Expected a comma but instead found ${rbracket.t}".right()
            }
            is None -> return "Expected a comma but instead found EOF".right()
        }
        return bytes.left()
    }

    fun writeByte(tokens: TokenStream): Either<UByteArray, String>{
        var bytes = ubyteArrayOf(InstructionSet.BYTE.code)
        val next = when(val next = tokens.next()){
            is Some -> next.t
            is None -> return "Expected an integer value but instead found EOF".right()
        }
        when(next){
            is Token.ByteLiteralToken -> bytes += next.literal.toUByte() and 0xFF.toUByte()
            is Token.ShortLiteralToken -> bytes += next.literal.toUByte()
            is Token.IntegerLiteralToken -> bytes += next.literal.toUByte()
            is Token.LongLiteralToken -> bytes += next.literal.toUByte()
            else -> return "Expected an integer literal but instead found $next".right()
        }
        return bytes.left()
    }

    fun writeWord(tokens: TokenStream): Either<UByteArray, String>{
        var bytes = ubyteArrayOf(InstructionSet.WORD.code)
        val next = when(val next = tokens.next()){
            is Some -> next.t
            is None -> return "Expected an integer value but instead found EOF".right()
        }
        when(next){
            is Token.ByteLiteralToken -> {
                bytes += 0.toUByte()
                bytes += next.literal.toUByte() and 0xFF.toUByte()
            }
            is Token.IntegerLiteralToken -> {
                bytes += ((next.literal ushr 2) and 0xff00).toUByte()
                bytes += (next.literal and 0x00ff).toUByte()
            }
            is Token.ShortLiteralToken -> {
                bytes += ((next.literal.toInt() ushr 2) and 0xff).toUByte()
                bytes += (next.literal and 0xff).toUByte()
            }
            is Token.LongLiteralToken -> {
                bytes += ((next.literal.toInt() ushr 2) and 0xff).toUByte()
                bytes += (next.literal and 0xff).toUByte()
            }
        }
        return bytes.left()
    }

    fun writeDoubleWord(tokens: TokenStream): Either<UByteArray, String>{
        var bytes = ubyteArrayOf(InstructionSet.WORD.code)
        val next = when(val next = tokens.next()){
            is Some -> next.t
            is None -> return "Expected an integer value but instead found EOF".right()
        }
        when(next){
            is Token.IntegerLiteralToken -> {
                bytes += 0.toUByte()
                bytes += ((next.literal and 0x00ff0000) shl 4).toUByte()
                bytes += ((next.literal and 0x0000ff00) shl 2).toUByte()
                bytes += (next.literal and 0x000000ff).toUByte()
            }
            else -> return "Expected an integer literal but instead found $next".right()
        }
        return bytes.left()
    }

    /**
     * TODO:
     *  The tokenizer currently does not recognize hexidecimal of any sort. The tokenizer needs to be able to recognize
     *  them in order for [writeQuadWord] and [writeDoubleWord] to work properly so for now these are WIP
     */
    fun writeQuadWord(tokens: TokenStream): Either<UByteArray, String>{
        var bytes = ubyteArrayOf(InstructionSet.WORD.code)
        val next = when(val next = tokens.next()){
            is Some -> next.t
            is None -> return "Expected an integer value but instead found EOF".right()
        }
        when(next){
            is Token.IntegerLiteralToken -> {
                when{
                    next.literal > 0xff -> {
                        bytes += 0.toUByte()
                        bytes += 0.toUByte()
                        bytes += ((next.literal and 0xff00) shl 2).toUByte()
                        bytes += (next.literal and 0x00ff).toUByte()
                    }
                    next.literal > 0xffff -> {
                        bytes += ((next.literal.toLong() and 0xff0000000000000) shl 16).toUByte()
                        bytes += ((next.literal.toLong() and 0x00ff000000) shl 12).toUByte()
                        bytes += ((next.literal and 0x0000ff00) shl 2).toUByte()
                        bytes += (next.literal and 0x000000ff).toUByte()
                        bytes += ((next.literal.toLong() and 0xff000000) shl 8).toUByte()
                        bytes += ((next.literal and 0x00ff0000) shl 4).toUByte()
                        bytes += ((next.literal and 0x0000ff00) shl 2).toUByte()
                        bytes += (next.literal and 0x000000ff).toUByte()
                    }
                    else -> {
                        bytes += 0.toUByte()
                        bytes += 0.toUByte()
                        bytes += 0.toUByte()
                        bytes += next.literal.toUByte()
                    }
                }
            }
            else -> return "Expected an integer literal but instead found $next".right()
        }
        return bytes.left()
    }

    private fun parseIntegerToBytes(token: Token): Either<UByteArray, String> {
        var bytes = ubyteArrayOf()
        return when (token){
            is Token.IntegerLiteralToken -> {
                bytes += InstructionSet.DWORD.code
                val bs = UByteArray(4)
                bs[3] = ((token.literal) and 0xFF).toUByte()
                bs[2] = ((token.literal ushr 8) and 0xFF).toUByte()
                bs[1] = ((token.literal ushr 16) and 0xFF).toUByte()
                bs[0] = ((token.literal ushr 24) and 0xFF).toUByte()
                bytes += bs
                bytes.left()
            }
            is Token.ByteLiteralToken -> {
                bytes += InstructionSet.BYTE.code
                bytes += token.literal.toUByte() and 0xFF.toUByte()
                bytes.left()
            }
            is Token.ShortLiteralToken -> {
                bytes += InstructionSet.WORD.code
                val bs = UByteArray(2)
                bs[1] = ((token.literal) and 0xFF).toUByte()
                bs[0] = ((token.literal.toInt() ushr 8) and 0xFF).toUByte()
                bytes += bs
                bytes.left()
            }
            is Token.LongLiteralToken -> {
                bytes += InstructionSet.QWORD.code
                val bs = UByteArray(8)
                bs[6] = ((token.literal) and 0xFF).toUByte()
                bs[5] = ((token.literal ushr 8) and 0xFF).toUByte()
                bs[4] = ((token.literal ushr 16) and 0xFF).toUByte()
                bs[3] = ((token.literal ushr 24) and 0xFF).toUByte()
                bs[2] = ((token.literal ushr 32) and 0xFF).toUByte()
                bs[1] = ((token.literal ushr 48) and 0xFF).toUByte()
                bs[0] = ((token.literal ushr 56) and 0xFF).toUByte()
                bytes += bs
                bytes.left()
            }
            else -> "Got non-integer token while trying to convert integer token to byte array: $token".right()
        }
    }

    private fun parsePush(tokens: TokenStream): Either<UByteArray, String>{
        val operand = when(val next = tokens.next()){
            is None -> return "Expected a left operand after PUSH instruction but instead found EOF".right()
            is Some -> next.t
        }
        var bytes = ubyteArrayOf(InstructionSet.PUSH.code)
        when(operand){
            is Token.IntegerLiteralToken,
            is Token.ByteLiteralToken,
            is Token.LongLiteralToken,
            is Token.ShortLiteralToken      -> bytes += when(val result = parseIntegerToBytes(operand)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IdentifierToken -> return "Labels are not yet implemented!".right()
        }
        return bytes.left()
    }

    private fun parseJump(tokens: TokenStream): Either<UByteArray, String>{
        val operand = when(val next = tokens.next()){
            is None -> return "Expected a left operand after JMP instruction but instead found EOF".right()
            is Some -> next.t
        }
        var bytes = ubyteArrayOf(InstructionSet.JMP.code)
        when(operand){
            is Token.IntegerLiteralToken -> bytes += operand.literal.toUByte()
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IdentifierToken -> {
                if(operand.lexeme !in labels){
                    return "Label ${operand.lexeme} does not exists".right()
                }
                bytes += labels[operand.lexeme]!!.toUByte()
            }
        }
        return bytes.left()
    }

    fun parseBinaryOperator(opcode: InstructionSet, tokens: TokenStream): Either<UByteArray, String>{
        val leftOperand = when(val next = tokens.next()){
            is None -> return "Expected a left operand after $opcode instruction but instead found EOF".right()
            is Some -> next.t
        }
        var bytes = ubyteArrayOf(opcode.code)
        when(leftOperand){
            is Token.IdentifierToken -> {
                val lexeme = leftOperand.lexeme.toUpperCase()
                when{
                    lexeme == "TOP" -> bytes += InstructionSet.TOP.code
                    lexeme.isSizeModifier -> {
                        when(lexeme){
                            "BYTE" -> bytes += when(val result = writeByte(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "WORD" -> bytes += when(val result = writeWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "DWORD" -> bytes += when(val result = writeDoubleWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "QWORD" -> bytes += when(val result = writeQuadWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            else -> {
                                if(leftOperand.lexeme !in labels){
                                    return "Label ${leftOperand.lexeme} does not exists".right()
                                }
                                bytes += labels[leftOperand.lexeme]!!.toUByte()
                            }
                        }
                    }
                }
            }
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IntegerLiteralToken -> bytes += leftOperand.literal.toUByte()
            is Token.ByteLiteralToken -> bytes += leftOperand.literal.toUByte() and 0xFF.toUByte()
            else -> return "Expected either a memory address destination or REF".right()
        }
        when(val next = tokens.next()){
            is Some -> when(next.t){
                is Token.CommaToken -> {}
                else -> return "Expected a comma but instead found ${next.t}".right()
            }
            is None -> return "Expected a comma but instead found EOF".right()
        }
        //Parse the right operand
        val rightOperand = when(val next = tokens.next()){
            is None -> return "Expected a right operand for $opcode instruction but instead found EOF".right()
            is Some -> next.t
        }
        when(rightOperand){
            is Token.IdentifierToken -> {
                val lexeme = rightOperand.lexeme.toUpperCase()
                when{
                    lexeme == "TOP" -> bytes += InstructionSet.TOP.code
                    lexeme.isSizeModifier -> {
                        when(lexeme){
                            "BYTE" -> bytes += when(val result = writeByte(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "WORD" -> bytes += when(val result = writeWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "DWORD" -> bytes += when(val result = writeDoubleWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "QWORD" -> bytes += when(val result = writeQuadWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                        }
                    }
                    else -> {
                        if(rightOperand.lexeme !in labels){
                            return "Label ${rightOperand.lexeme} does not exists".right()
                        }
                        bytes += labels[rightOperand.lexeme]!!.toUByte()
                    }
                }
            }
            //TODO: See [parseRef]
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IntegerLiteralToken, is Token.LongLiteralToken, is Token.ByteLiteralToken, is Token.ShortLiteralToken ->
                bytes += when(val result = parseIntegerToBytes(rightOperand)){
                    is Either.Left -> result.a
                    is Either.Right -> return result
                }
            else -> return "Expected either a memory address destination or REF".right()
        }
        return bytes.left()
    }

    fun parseTrinaryOperator(opcode: InstructionSet, tokens: TokenStream): Either<UByteArray, String>{
        val leftOperand = when(val next = tokens.next()){
            is None -> return "Expected a left operand after $opcode instruction but instead found EOF".right()
            is Some -> next.t
        }
        var bytes = ubyteArrayOf(opcode.code)
        when(leftOperand){
            is Token.IdentifierToken -> {
                val lexeme = leftOperand.lexeme.toUpperCase()
                when{
                    lexeme == "TOP" -> bytes += InstructionSet.TOP.code
                    lexeme.isSizeModifier -> {
                        when(lexeme){
                            "BYTE" -> bytes += when(val result = writeByte(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "WORD" -> bytes += when(val result = writeWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "DWORD" -> bytes += when(val result = writeDoubleWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "QWORD" -> bytes += when(val result = writeQuadWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            else -> {
                                if(leftOperand.lexeme !in labels){
                                    return "Label ${leftOperand.lexeme} does not exists".right()
                                }
                                bytes += labels[leftOperand.lexeme]!!.toUByte()
                            }
                        }
                    }
                }
            }
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IntegerLiteralToken -> bytes += leftOperand.literal.toUByte()
            is Token.ByteLiteralToken -> bytes += leftOperand.literal.toUByte() and 0xFF.toUByte()
            else -> return "Expected either a memory address destination or REF".right()
        }
        when(val next = tokens.next()){
            is Some -> when(next.t){
                is Token.CommaToken -> {}
                else -> return "Expected a comma but instead found ${next.t}".right()
            }
            is None -> return "Expected a comma but instead found EOF".right()
        }
        //Parse the right operand
        val middleOperand = when(val next = tokens.next()){
            is None -> return "Expected a middle operand for $opcode instruction but instead found EOF".right()
            is Some -> next.t
        }
        when(middleOperand){
            is Token.IdentifierToken -> {
                val lexeme = middleOperand.lexeme.toUpperCase()
                when{
                    lexeme == "TOP" -> return "TOP is not a valid destination. Use PUSH instead!".right()
                    lexeme.isSizeModifier -> {
                        when(lexeme){
                            "BYTE" -> bytes += when(val result = writeByte(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "WORD" -> bytes += when(val result = writeWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "DWORD" -> bytes += when(val result = writeDoubleWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "QWORD" -> bytes += when(val result = writeQuadWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            else -> {
                                if(middleOperand.lexeme !in labels){
                                    return "Label ${middleOperand.lexeme} does not exists".right()
                                }
                                bytes += labels[middleOperand.lexeme]!!.toUByte()
                            }
                        }
                    }
                    else -> return "Labels are not yet implemented!".right()
                }
            }
            //TODO: See [parseRef]
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IntegerLiteralToken, is Token.LongLiteralToken, is Token.ByteLiteralToken, is Token.ShortLiteralToken ->
                bytes += when(val result = parseIntegerToBytes(middleOperand)){
                    is Either.Left -> result.a
                    is Either.Right -> return result
                }
            else -> return "Expected either a memory address destination or REF".right()
        }
        when(val next = tokens.next()){
            is Some -> when(next.t){
                is Token.CommaToken -> {}
                else -> return "Expected a comma but instead found ${next.t}".right()
            }
            is None -> return "Expected a comma but instead found EOF".right()
        }
        val rightOperand = when(val next = tokens.next()){
            is None -> return "Expected a right operand for $opcode instruction but instead found EOF".right()
            is Some -> next.t
        }
        when(rightOperand){
            is Token.IdentifierToken -> {
                val lexeme = rightOperand.lexeme.toUpperCase()
                when{
                    lexeme == "TOP" -> return "TOP is not a valid destination. Use PUSH instead!".right()
                    lexeme.isSizeModifier -> {
                        when(lexeme){
                            "BYTE" -> bytes += when(val result = writeByte(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "WORD" -> bytes += when(val result = writeWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "DWORD" -> bytes += when(val result = writeDoubleWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                            "QWORD" -> bytes += when(val result = writeQuadWord(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return result
                            }
                        }
                    }
                    else -> {
                        if(rightOperand.lexeme !in labels){
                            return "Label ${rightOperand.lexeme} does not exists".right()
                        }
                        bytes += labels[rightOperand.lexeme]!!.toUByte()
                    }
                }
            }
            //TODO: See [parseRef]
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IntegerLiteralToken, is Token.LongLiteralToken, is Token.ByteLiteralToken, is Token.ShortLiteralToken ->
                bytes += when(val result = parseIntegerToBytes(middleOperand)){
                    is Either.Left -> result.a
                    is Either.Right -> return result
                }
            else -> return "Expected either a memory address destination or REF".right()
        }
        return bytes.left()
    }

    private fun parseLabel(ident: Token.IdentifierToken): Option<String>{
        val lexeme = ident.lexeme
        if(lexeme in labels){
            return "Label $lexeme already exists".some()
        }
        labels += lexeme to bytes.size
        return none()
    }

    private fun createErrorMessage(pos: TokenPos, msg: String): String =
        "${pos.pos.line}:${pos.pos.col}: $msg"

    private fun parseFile(): Either<Executable, String>{
        val tokens = when(val result = lexer.tokenize()){
            is Some -> result.t
            is None -> return "Failed to tokenize file, see console.".right()
        }
        while(tokens.hasNext()){
            when(val next = tokens.next()){
                is Some -> when(next.t){
                    is Token.IdentifierToken -> {
                        val ident = next.t as Token.IdentifierToken
                        when(ident.lexeme.toUpperCase()){
                            "MOV" -> bytes += when(val result = parseBinaryOperator(InstructionSet.MOVE, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "MOVB" -> bytes += when(val result = parseBinaryOperator(InstructionSet.MOVB, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "MOVW" -> bytes += when(val result = parseBinaryOperator(InstructionSet.MOVW, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "MOVD" -> bytes += when(val result = parseBinaryOperator(InstructionSet.MOVD, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "MOQ" -> bytes += when(val result = parseBinaryOperator(InstructionSet.MOVQ, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "PUSH" -> bytes += when(val result = parsePush(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "POP" -> bytes += InstructionSet.POP.code
                            "JMP" -> bytes += when(val result = parseJump(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "ADD" -> bytes += when(val result = parseBinaryOperator(InstructionSet.ADD, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "SUB" -> bytes += when(val result = parseBinaryOperator(InstructionSet.SUB, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "MUL" -> bytes += when(val result = parseBinaryOperator(InstructionSet.MUL, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "DIV" -> bytes += when(val result = parseBinaryOperator(InstructionSet.DIV, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "LE" -> bytes += when(val result = parseBinaryOperator(InstructionSet.LE, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "LT" ->  bytes += when(val result = parseBinaryOperator(InstructionSet.LT, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "GT" ->  bytes += when(val result = parseBinaryOperator(InstructionSet.GT, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "GE" ->  bytes += when(val result = parseBinaryOperator(InstructionSet.GE, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "JEQ" ->  bytes += when(val result = parseTrinaryOperator(InstructionSet.JEQ, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "JLT" ->  bytes += when(val result = parseTrinaryOperator(InstructionSet.JLT, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "JLE" ->  bytes += when(val result = parseTrinaryOperator(InstructionSet.JLE, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "JGT" ->  bytes += when(val result = parseTrinaryOperator(InstructionSet.JGT, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "JGE" ->  bytes += when(val result = parseTrinaryOperator(InstructionSet.JGT, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "AND" -> bytes += when(val result = parseBinaryOperator(InstructionSet.AND, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "OR" -> bytes += when(val result = parseBinaryOperator(InstructionSet.OR, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "XOR" -> bytes += when(val result = parseBinaryOperator(InstructionSet.XOR, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "SHR" -> bytes += when(val result = parseBinaryOperator(InstructionSet.SHR, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "SHL" -> bytes += when(val result = parseBinaryOperator(InstructionSet.SHL, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            "INV" -> bytes += when(val result = parseBinaryOperator(InstructionSet.INV, tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return createErrorMessage(ident.startPos, result.b).right()
                            }
                            else -> {
                                if(tokens.peek is Some){
                                    val peek = (tokens.peek as Some).t
                                    if(peek is Token.ColonToken){
                                        when(val result = parseLabel(ident)){
                                            is Some -> return createErrorMessage(ident.startPos, result.t).right()
                                            is None -> {}
                                        }
                                    }
                                }else{
                                    return createErrorMessage(ident.startPos, "Expected a token after identifier but instead found EOF").right()
                                }
                            }
                        }
                    }
                }
            }
        }
        return Executable(bytes).left()
    }
}