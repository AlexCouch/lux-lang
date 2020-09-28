import arrow.core.*
import java.io.File

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
    private fun parseRef(tokens: TokenStream): Either<ByteArray, String>{
        val next = when(val next = tokens.next()){
            is None -> return "Expected an operand after MOV instruction but instead found EOF".right()
            is Some -> next.t
        }
        var bytes = byteArrayOf(InstructionSet.REF.code)
        when(next){
            is Token.IntegerLiteralToken -> {
                bytes += InstructionSet.BYTE.code
                bytes += next.literal.toByte()
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

    fun writeByte(tokens: TokenStream): Either<ByteArray, String>{
        var bytes = byteArrayOf(InstructionSet.BYTE.code)
        val next = when(val next = tokens.next()){
            is Some -> next.t
            is None -> return "Expected an integer value but instead found EOF".right()
        }
        when(next){
            is Token.IntegerLiteralToken -> {
                bytes += next.literal.toByte()
            }
            else -> return "Expected an integer literal but instead found $next".right()
        }
        return bytes.left()
    }

    fun writeWord(tokens: TokenStream): Either<ByteArray, String>{
        var bytes = byteArrayOf(InstructionSet.WORD.code)
        val next = when(val next = tokens.next()){
            is Some -> next.t
            is None -> return "Expected an integer value but instead found EOF".right()
        }
        when(next){
            is Token.IntegerLiteralToken -> {
                when{
                    next.literal > 0xff -> {
                        bytes += ((next.literal and 0xff00) ushr 2).toByte()
                        bytes += (next.literal and 0x00ff).toByte()
                    }
                    next.literal > 0xffff -> {
                        return "Expecting integer of size word or less but instead found size greater than word: ${next.literal}".right()
                    }
                    else -> {
                        bytes += 0
                        bytes += next.literal.toByte()
                    }
                }
            }
            else -> return "Expected an integer literal but instead found $next".right()
        }
        return bytes.left()
    }

    fun writeDoubleWord(tokens: TokenStream): Either<ByteArray, String>{
        var bytes = byteArrayOf(InstructionSet.WORD.code)
        val next = when(val next = tokens.next()){
            is Some -> next.t
            is None -> return "Expected an integer value but instead found EOF".right()
        }
        when(next){
            is Token.IntegerLiteralToken -> {
                when{
                    next.literal > 0xff -> {
                        bytes += ((next.literal and 0xff00) shl 2).toByte()
                        bytes += (next.literal and 0x00ff).toByte()
                    }
                    next.literal > 0xffff -> {
                        bytes += ((next.literal.toLong() and 0xff000000) shl 8).toByte()
                        bytes += ((next.literal and 0x00ff0000) shl 4).toByte()
                        bytes += ((next.literal and 0x0000ff00) shl 2).toByte()
                        bytes += (next.literal and 0x000000ff).toByte()
                    }
                    else -> bytes += next.literal.toByte()
                }
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
    fun writeQuadWord(tokens: TokenStream): Either<ByteArray, String>{
        var bytes = byteArrayOf(InstructionSet.WORD.code)
        val next = when(val next = tokens.next()){
            is Some -> next.t
            is None -> return "Expected an integer value but instead found EOF".right()
        }
        when(next){
            is Token.IntegerLiteralToken -> {
                when{
                    next.literal > 0xff -> {
                        bytes += 0
                        bytes += 0
                        bytes += ((next.literal and 0xff00) shl 2).toByte()
                        bytes += (next.literal and 0x00ff).toByte()
                    }
                    next.literal > 0xffff -> {
                        bytes += ((next.literal.toLong() and 0xff0000000000000) shl 16).toByte()
                        bytes += ((next.literal.toLong() and 0x00ff000000) shl 12).toByte()
                        bytes += ((next.literal and 0x0000ff00) shl 2).toByte()
                        bytes += (next.literal and 0x000000ff).toByte()
                        bytes += ((next.literal.toLong() and 0xff000000) shl 8).toByte()
                        bytes += ((next.literal and 0x00ff0000) shl 4).toByte()
                        bytes += ((next.literal and 0x0000ff00) shl 2).toByte()
                        bytes += (next.literal and 0x000000ff).toByte()
                    }
                    else -> {
                        bytes += 0
                        bytes += 0
                        bytes += 0
                        bytes += next.literal.toByte()
                    }
                }
            }
            else -> return "Expected an integer literal but instead found $next".right()
        }
        return bytes.left()
    }

    /**
     * Parses a MOV instruction
     *
     * Example:
     *  ```
     *   MOV    0, 10 ;Move The integer 10 into 0
     *  ```
     *  The left and right operands may be integers or they may be a reference to a label, with or without square brackets.
     *  @see labels
     */
    private fun parseMove(tokens: TokenStream): Either<ByteArray, String> {
        val leftOperand = when(val next = tokens.next()){
            is None -> return "Expected a left operand after MOV instruction but instead found EOF".right()
            is Some -> next.t
        }
        var bytes = byteArrayOf(InstructionSet.MOVE.code)
        //Parse the left operand
        when(leftOperand){
            is Token.IdentifierToken -> {
                val lexeme = leftOperand.lexeme.toUpperCase()
                when{
                    lexeme == "TOP" -> return "TOP is not a valid destination. Use PUSH instead!".right()
                    lexeme.isSizeModifier -> return "Operand size modifiers are not allowed on destinations".right()
                }
            }
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IntegerLiteralToken -> {
                bytes += leftOperand.literal.toByte()
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
        //Parse the right operand
        val rightOperand = when(val next = tokens.next()){
            is None -> return "Expected a right operand for MOV instruction but instead found EOF".right()
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
                    else -> return "Labels are not yet implemented!".right()
                }
            }
            //TODO: See [parseRef]
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IntegerLiteralToken -> {
                bytes += rightOperand.literal.toByte()
            }
            else -> return "Expected either a memory address destination or REF".right()
        }
        return bytes.left()
    }

    private fun parseMoveb(tokens: TokenStream): Either<ByteArray, String>{
        val leftOperand = when(val next = tokens.next()){
            is None -> return "Expected a left operand after MOV instruction but instead found EOF".right()
            is Some -> next.t
        }
        var bytes = byteArrayOf(InstructionSet.MOVB.code)
        //Parse the left operand
        when(leftOperand){
            is Token.IdentifierToken -> {
                val lexeme = leftOperand.lexeme.toUpperCase()
                when{
                    lexeme == "TOP" -> return "TOP is not a valid destination. Use PUSH instead!".right()
                    lexeme.isSizeModifier -> return "Operand size modifiers are not allowed on destinations".right()
                }
            }
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IntegerLiteralToken -> {
                bytes += leftOperand.literal.toByte()
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
        //Parse the right operand
        val rightOperand = when(val next = tokens.next()){
            is None -> return "Expected a right operand for MOV instruction but instead found EOF".right()
            is Some -> next.t
        }
        when(rightOperand){
            is Token.IdentifierToken -> {
                val lexeme = rightOperand.lexeme.toUpperCase()
                when{
                    lexeme == "TOP" -> return "TOP is not a valid destination. Use PUSH instead!".right()
                    lexeme.isSizeModifier -> return "No size modifiers allowed on movb operand: $lexeme".right()
                    else -> return "Labels are not yet implemented!".right()
                }
            }
            //TODO: See [parseRef]
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IntegerLiteralToken -> {
                bytes += rightOperand.literal.toByte()
            }
            else -> return "Expected either a memory address destination or REF".right()
        }
        return bytes.left()
    }

    private fun parseMovew(tokens: TokenStream): Either<ByteArray, String>{
        val leftOperand = when(val next = tokens.next()){
            is None -> return "Expected a left operand after MOV instruction but instead found EOF".right()
            is Some -> next.t
        }
        var bytes = byteArrayOf(InstructionSet.MOVW.code)
        //Parse the left operand
        when(leftOperand){
            is Token.IdentifierToken -> {
                val lexeme = leftOperand.lexeme.toUpperCase()
                when{
                    lexeme == "TOP" -> return "TOP is not a valid destination. Use PUSH instead!".right()
                    lexeme.isSizeModifier -> return "Operand size modifiers are not allowed on destinations".right()
                }
            }
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IntegerLiteralToken -> {
                bytes += leftOperand.literal.toByte()
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
        //Parse the right operand
        val rightOperand = when(val next = tokens.next()){
            is None -> return "Expected a right operand for MOV instruction but instead found EOF".right()
            is Some -> next.t
        }
        when(rightOperand){
            is Token.IdentifierToken -> {
                val lexeme = rightOperand.lexeme.toUpperCase()
                when{
                    lexeme == "TOP" -> return "TOP is not a valid destination. Use PUSH instead!".right()
                    lexeme.isSizeModifier -> return "No size modifiers allowed on movb operand: $lexeme".right()
                    else -> return "Labels are not yet implemented!".right()
                }
            }
            //TODO: See [parseRef]
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IntegerLiteralToken -> {
//                bytes += InstructionSet.WORD.code
                val bs = ByteArray(2)
                bs[1] = ((rightOperand.literal) and 0xFF).toByte()
                bs[0] = ((rightOperand.literal ushr 4) and 0xFF).toByte()
                bytes += bs
            }
            else -> return "Expected either a memory address destination or REF".right()
        }
        return bytes.left()
    }

    private fun parseFile(): Either<Executable, String>{
        val tokens = lexer.tokenize()
        var bytes = byteArrayOf()
        while(tokens.hasNext()){
            when(val next = tokens.next()){
                is Some -> when(next.t){
                    is Token.IdentifierToken -> {
                        val ident = next.t as Token.IdentifierToken
                        when(ident.lexeme.toUpperCase()){
                            "MOV" -> bytes += when(val result = parseMove(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return "${ident.startPos.pos.line}:${ident.startPos.pos.col}: ${result.b}".right()
                            }
                            "MOVB" -> bytes += when(val result = parseMoveb(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return "${ident.startPos.pos.line}:${ident.startPos.pos.col}: ${result.b}".right()
                            }
                            "MOVW" -> bytes += when(val result = parseMovew(tokens)){
                                is Either.Left -> result.a
                                is Either.Right -> return "${ident.startPos.pos.line}:${ident.startPos.pos.col}: ${result.b}".right()
                            }
                        }
                    }
                }
            }
        }
        return Executable(bytes).left()
    }
}