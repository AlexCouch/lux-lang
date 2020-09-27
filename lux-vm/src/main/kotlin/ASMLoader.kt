import arrow.core.*
import java.io.File

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
    private fun parseRef(tokens: TokenStream): Either<IntArray, String>{
        val next = when(val next = tokens.next()){
            is None -> return "Expected an operand after MOV instruction but instead found EOF".right()
            is Some -> next.t
        }
        var bytes = intArrayOf(InstructionSet.REF.code)
        when(next){
            is Token.IntegerLiteralToken -> {
                bytes += next.literal
            }
            else -> return "Expected a memory address to reference, found $next".right()
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
    private fun parseMove(tokens: TokenStream): Either<IntArray, String> {
        val leftOperand = when(val next = tokens.next()){
            is None -> return "Expected a left operand after MOV instruction but instead found EOF".right()
            is Some -> next.t
        }
        var bytes = intArrayOf(InstructionSet.MOVE.code)
        //Parse the left operand
        when(leftOperand){
            is Token.IdentifierToken -> {
                when(leftOperand.lexeme.toUpperCase()){
                    "TOP" -> return "TOP is not a valid destination. Use PUSH instead!".right()
                }
            }
            is Token.LBracketToken -> return "A reference is not a valid destination. Remove the reference brackets and try again!".right()
            is Token.IntegerLiteralToken -> {
                bytes += leftOperand.literal
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
                when(rightOperand.lexeme.toUpperCase()){
                    "TOP" -> return "TOP is not a valid destination. Use PUSH instead!".right()
                }
            }
            //TODO: See [parseRef]
            is Token.LBracketToken -> bytes += when(val result = parseRef(tokens)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            is Token.IntegerLiteralToken -> {
                bytes += rightOperand.literal
            }
            else -> return "Expected either a memory address destination or REF".right()
        }
        return bytes.left()
    }

    private fun parseFile(): Either<Executable, String>{
        val tokens = lexer.tokenize()
        var bytes = intArrayOf()
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
                        }
                    }
                }
            }
        }
        return Executable(bytes).left()
    }
}