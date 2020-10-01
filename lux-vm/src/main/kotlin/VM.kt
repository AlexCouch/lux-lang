import java.io.File


class Executable(private val instructions: ByteArray): Iterator<Byte>{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Executable

        if (!instructions.contentEquals(other.instructions)) return false

        return true
    }

    var instructionPtr = 0

    override fun hashCode(): Int {
        return instructions.contentHashCode()
    }

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            instructions.withIndex().forEach { (idx, it) ->
                append("0x${Integer.toHexString(it.toInt())}")
                if(idx < instructions.size - 1){
                    append(", ")
                }
            }
        }

    override fun hasNext(): Boolean = instructionPtr < instructions.size

    override fun next(): Byte = instructions[instructionPtr++]

    fun nextByte() = DataType.Byte(instructions[instructionPtr++])
    fun nextWord() = DataType.Word(DataType.Byte(instructions[instructionPtr++]), DataType.Byte(instructions[instructionPtr++]))
    fun nextDoubleWord() = DataType.DoubleWord(
        DataType.Word(
            DataType.Byte(instructions[instructionPtr++]),
            DataType.Byte(instructions[instructionPtr++])
        ),
        DataType.Word(
            DataType.Byte(instructions[instructionPtr++]),
            DataType.Byte(instructions[instructionPtr++])
        )
    )

    fun nextQuadWord() = DataType.QuadWord(
        DataType.DoubleWord(
            DataType.Word(
                DataType.Byte(instructions[instructionPtr++]),
                DataType.Byte(instructions[instructionPtr++])
            ),
            DataType.Word(
                DataType.Byte(instructions[instructionPtr++]),
                DataType.Byte(instructions[instructionPtr++])
            )
        ),
        DataType.DoubleWord(
            DataType.Word(
                DataType.Byte(instructions[instructionPtr++]),
                DataType.Byte(instructions[instructionPtr++])
            ),
            DataType.Word(
                DataType.Byte(instructions[instructionPtr++]),
                DataType.Byte(instructions[instructionPtr++])
            )
        )
    )

    fun writeToFile(file: File){
        file.writeBytes(instructions)
    }

}

class Stack{
    private val stack: ByteArray = ByteArray(1024)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Stack

        if (!stack.contentEquals(other.stack)) return false

        return true
    }

    private var stackPtr = 0

    val top: Byte
        get() = if(stackPtr == 0) 0 else stack[stackPtr - 1]

    override fun hashCode(): Int {
        return stack.contentHashCode()
    }

    fun push(data: DataType){
        when(data){
            is DataType.Byte -> stack[stackPtr++] = data.data
            is DataType.Word -> {
                stack[stackPtr++] = data.data1.data
                stack[stackPtr++] = data.data2.data
            }
            is DataType.DoubleWord -> {
                stack[stackPtr++] = data.data1.data1.data
                stack[stackPtr++] = data.data1.data2.data
                stack[stackPtr++] = data.data2.data1.data
                stack[stackPtr++] = data.data2.data2.data
            }
            is DataType.QuadWord -> {
                stack[stackPtr++] = data.data1.data1.data1.data
                stack[stackPtr++] = data.data1.data1.data2.data
                stack[stackPtr++] = data.data1.data2.data1.data
                stack[stackPtr++] = data.data1.data2.data2.data
                stack[stackPtr++] = data.data2.data1.data1.data
                stack[stackPtr++] = data.data2.data1.data2.data
                stack[stackPtr++] = data.data2.data2.data1.data
                stack[stackPtr++] = data.data2.data2.data2.data
            }
        }
    }

    fun pop(){
        stack[--stackPtr] = 0
    }

}

class Memory{
    val memory = ByteArray(1024)

    fun write(dest: Byte, target: DataType){
        when(target){
            is DataType.Byte -> writeByte(dest, target)
            is DataType.Word -> writeWord(dest, target)
            is DataType.DoubleWord -> writeDouble(dest, target)
            is DataType.QuadWord -> writeQuad(dest, target)
        }
    }

    fun writeByte(dest: Byte, target: DataType.Byte){
        memory[dest.toInt()] = target.data
    }

    fun writeWord(dest: Byte, target: DataType.Word){
        memory[dest.toInt()] = target.data1.data
        memory[dest.toInt() + 1] = target.data2.data
    }

    fun writeDouble(dest: Byte, target: DataType.DoubleWord){
        memory[dest.toInt()] = target.data1.data1.data
        memory[dest.toInt()+1] = target.data1.data2.data
        memory[dest.toInt()+2] = target.data2.data1.data
        memory[dest.toInt()+3] = target.data2.data2.data
    }
    fun writeQuad(dest: Byte, target: DataType.QuadWord){
        memory[dest.toInt()] = target.data1.data1.data1.data
        memory[dest.toInt()+1] = target.data1.data1.data2.data
        memory[dest.toInt()+2] = target.data1.data2.data1.data
        memory[dest.toInt()+3] = target.data1.data2.data2.data
        memory[dest.toInt()+4] = target.data2.data1.data1.data
        memory[dest.toInt()+5] = target.data2.data1.data2.data
        memory[dest.toInt()+6] = target.data2.data2.data1.data
        memory[dest.toInt()+7] = target.data2.data2.data2.data
    }

    fun readByte(dest: Byte) = DataType.Byte(memory[dest.toInt()])
    fun readWord(dest: Byte) = DataType.Word(DataType.Byte(memory[dest.toInt()]), DataType.Byte(memory[dest.toInt()+1]))
    fun readDoubleWord(dest: Byte) = DataType.DoubleWord(
        DataType.Word(
            DataType.Byte(memory[dest.toInt()]),
            DataType.Byte(memory[dest.toInt()+1])
        ),
        DataType.Word(
            DataType.Byte(memory[dest.toInt()+2]),
            DataType.Byte(memory[dest.toInt()+3])
        )
    )

    fun readQuadWord(dest: Byte) = DataType.QuadWord(
        DataType.DoubleWord(
            DataType.Word(
                DataType.Byte(memory[dest.toInt()]),
                DataType.Byte(memory[dest.toInt()+1])
            ),
            DataType.Word(
                DataType.Byte(memory[dest.toInt()+2]),
                DataType.Byte(memory[dest.toInt()+3])
            )
        ),
        DataType.DoubleWord(
            DataType.Word(
                DataType.Byte(memory[dest.toInt()+4]),
                DataType.Byte(memory[dest.toInt()+5])
            ),
            DataType.Word(
                DataType.Byte(memory[dest.toInt()+6]),
                DataType.Byte(memory[dest.toInt()+7])
            )
        )
    )
}

@ExperimentalStdlibApi
class VM(val binary: Executable){
    val memory = Memory()
    val stack = Stack()

    private fun reference(): DataType {
        val next = binary.next()
        val nextInstr = InstructionSet.values().find { it.code == next }
        return when{
            nextInstr != null -> {
                when(nextInstr){
                    InstructionSet.BYTE -> memory.readByte(binary.next())
                    InstructionSet.WORD -> memory.readWord(binary.next())
                    InstructionSet.DWORD -> memory.readDoubleWord(binary.next())
                    InstructionSet.QWORD -> memory.readQuadWord(binary.next())
                    else -> memory.readByte(next)
                }
            }
            else -> memory.readByte(next)
        }
    }

    private fun push(){
        val data = binary.nextByte()
        val dataInstr = InstructionSet.values().find { it.code == data.data }
        val dataVal = if(dataInstr != null){
            when(dataInstr){
                InstructionSet.REF -> reference()
                else -> data
            }
        }else{
            data
        }
        stack.push(dataVal)
    }

    private fun pop(){
        stack.pop()
    }

    private fun jump(){
        val location = binary.next()
        val locInstr = InstructionSet.values().find { it.code == location }
        if(locInstr == null){
            binary.instructionPtr = location.toInt()
        }
        when(locInstr){
            InstructionSet.TOP -> binary.instructionPtr = stack.top.toInt()
        }
    }

    private fun unaryOperator(block: (operand: DataType) -> Unit){
        val operand = binary.nextByte()
        val operandInstr = InstructionSet.values().find { it.code == operand.data }
        val operandValue = if(operandInstr != null){
            when(operandInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val ref = reference()
                    if(ref !is DataType.Byte){
                        println("Expected a destination of size byte but instead found size $ref")
                        return
                    }
                    ref
                }
                else -> operand
            }
        }else{
            operand
        }
        block(operandValue)
    }

    fun binaryOperation(block: (left: DataType, right: DataType) -> Unit){
        val left = binary.nextByte()
        val leftInstr = InstructionSet.values().find { it.code == left.data }
        val leftValue = if(leftInstr != null){
            when(leftInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val ref = reference()
                    if(ref !is DataType.Byte){
                        println("Expected a destination of size byte but instead found size $ref")
                        return
                    }
                    ref
                }
                else -> left
            }
        }else{
            left
        }
        val right = binary.nextByte()
        val rightInstr = InstructionSet.values().find { it.code == right.data }
        val rightValue = if(rightInstr != null){
            when(rightInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val ref = reference()
                    if(ref !is DataType.Byte){
                        println("Expected a destination of size byte but instead found size $ref")
                        return
                    }
                    ref
                }
                InstructionSet.BYTE -> binary.nextByte()
                InstructionSet.WORD -> binary.nextWord()
                InstructionSet.DWORD -> binary.nextDoubleWord()
                InstructionSet.QWORD -> binary.nextQuadWord()
                else -> right
            }
        }else{
            right
        }
        block(leftValue, rightValue)
    }

    fun trinaryOperation(block: (left: DataType, middle: DataType, right: DataType) -> Unit){
        val left = binary.nextByte()
        val leftInstr = InstructionSet.values().find { it.code == left.data }
        val leftValue = if(leftInstr != null){
            when(leftInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val ref = reference()
                    if(ref !is DataType.Byte){
                        println("Expected a destination of size byte but instead found size $ref")
                        return
                    }
                    ref
                }
                else -> left
            }
        }else{
            left
        }
        val middle = binary.nextByte()
        val middleInstr = InstructionSet.values().find { it.code == middle.data }
        val middleValue = if(middleInstr != null){
            when(middleInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val ref = reference()
                    if(ref !is DataType.Byte){
                        println("Expected a destination of size byte but instead found size $ref")
                        return
                    }
                    ref
                }
                InstructionSet.BYTE -> binary.nextByte()
                InstructionSet.WORD -> binary.nextWord()
                InstructionSet.DWORD -> binary.nextDoubleWord()
                InstructionSet.QWORD -> binary.nextQuadWord()
                else -> middle
            }
        }else{
            middle
        }
        val right = binary.nextByte()
        val rightInstr = InstructionSet.values().find { it.code == right.data }
        val rightValue = if(rightInstr != null){
            when(rightInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val ref = reference()
                    if(ref !is DataType.Byte){
                        println("Expected a destination of size byte but instead found size $ref")
                        return
                    }
                    ref
                }
                InstructionSet.BYTE -> binary.nextByte()
                InstructionSet.WORD -> binary.nextWord()
                InstructionSet.DWORD -> binary.nextDoubleWord()
                InstructionSet.QWORD -> binary.nextQuadWord()
                else -> right
            }
        }else{
            right
        }
        block(leftValue, middleValue, rightValue)
    }

    fun run(){
        while(binary.hasNext()){
            val next = binary.next()
            val instr = InstructionSet.values().find { it.code == next }
            if(instr == null){
                println("Invalid opcode: $next")
                return
            }
            when(instr){
                InstructionSet.MOVE -> binaryOperation { left, right ->
                    if(right !is DataType.Byte){
                        println("Expected a data type of byte for right operand but instead got $right")
                        return@binaryOperation
                    }
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }

                    memory.writeByte(left.data, memory.readByte(right.data))
                }
                InstructionSet.MOVB -> binaryOperation { left, right ->
                    if(right !is DataType.Byte){
                        println("Expected a data type of byte for right operand but instead got $right")
                        return@binaryOperation
                    }
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    memory.writeByte(left.data, right)
                }
                InstructionSet.MOVW -> binaryOperation{ left, right ->
                    if(right !is DataType.Word){
                        println("Expected a data type of byte for right operand but instead got $right")
                        return@binaryOperation
                    }
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    memory.writeWord(left.data, right)
                }
                InstructionSet.MOVD -> binaryOperation{ left, right ->
                    if(right !is DataType.DoubleWord){
                        println("Expected a data type of byte for right operand but instead got $right")
                        return@binaryOperation
                    }
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    memory.writeDouble(left.data, right)
                }
                InstructionSet.MOVQ -> binaryOperation{ left, right ->
                    if(right !is DataType.QuadWord){
                        println("Expected a data type of byte for right operand but instead got $right")
                        return@binaryOperation
                    }
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    memory.writeQuad(left.data, right)
                }
                InstructionSet.PUSH -> unaryOperator{ operand ->
                    stack.push(operand)
                }
                InstructionSet.POP -> pop()
                InstructionSet.JMP -> unaryOperator { operand ->
                    if(operand !is DataType.Byte){
                        println("Expected operand to be of size data but was instead $operand")
                        return@unaryOperator
                    }
                    binary.instructionPtr = operand.data.toInt()
                }
                InstructionSet.ADD -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    val sum = memory.readByte(left.data) + right
                    memory.writeByte(left.data, sum)
                }
                InstructionSet.SUB -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    val diff = memory.readByte(left.data) - right
                    memory.writeByte(left.data, diff)
                }
                InstructionSet.MUL -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    val product = memory.readByte(left.data) * right
                    memory.writeByte(left.data, product)
                }
                InstructionSet.DIV -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    val quotient = memory.readByte(left.data) / right
                    memory.writeByte(left.data, quotient)
                }
                InstructionSet.LE -> binaryOperation{ left, right ->
                    val cmp = left <= right
                    stack.push(DataType.Byte(if(cmp) 1 else 0))
                }
                InstructionSet.LT -> binaryOperation{ left, right ->
                    val cmp = left < right
                    stack.push(DataType.Byte(if(cmp) 1 else 0))
                }
                InstructionSet.GE -> binaryOperation{ left, right ->
                    val cmp = left >= right
                    stack.push(DataType.Byte(if(cmp) 1 else 0))
                }
                InstructionSet.GT -> binaryOperation{ left, right ->
                    val cmp = left > right
                    stack.push(DataType.Byte(if(cmp) 1 else 0))
                }
                InstructionSet.JEQ -> trinaryOperation{ left, middle, right ->
                    val cmp = left == middle
                    if(right !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@trinaryOperation
                    }
                    if(!cmp){
                        return@trinaryOperation
                    }
                    binary.instructionPtr = right.data.toInt()
                }
                InstructionSet.JLE -> trinaryOperation{ left, middle, right ->
                    val cmp = left <= middle
                    if(right !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@trinaryOperation
                    }
                    if(!cmp){
                        return@trinaryOperation
                    }
                    binary.instructionPtr = right.data.toInt()
                }
                InstructionSet.JLT -> trinaryOperation{ left, middle, right ->
                    val cmp = left < middle
                    if(right !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@trinaryOperation
                    }
                    if(!cmp){
                        return@trinaryOperation
                    }
                    binary.instructionPtr = right.data.toInt()
                }
                InstructionSet.JGT -> trinaryOperation{ left, middle, right ->
                    val cmp = left > middle
                    if(right !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@trinaryOperation
                    }
                    if(!cmp){
                        return@trinaryOperation
                    }
                    binary.instructionPtr = right.data.toInt()
                }
                InstructionSet.JGE -> trinaryOperation{ left, middle, right ->
                    val cmp = left >= middle
                    if(right !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@trinaryOperation
                    }
                    if(!cmp){
                        return@trinaryOperation
                    }
                    binary.instructionPtr = right.data.toInt()
                }
                InstructionSet.AND -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a destination of size byte but instead found $left")
                        return@binaryOperation
                    }
                    when(right){
                        is DataType.Byte -> {
                            val result = memory.readByte(left.data) and right
                            memory.writeByte(left.data, result)
                        }
                        is DataType.Word -> {
                            val result = memory.readWord(left.data) and right
                            memory.writeWord(left.data, result)
                        }
                        is DataType.DoubleWord -> {
                            val result = memory.readDoubleWord(left.data) and right
                            memory.writeDouble(left.data, result)
                        }
                        is DataType.QuadWord -> {
                            val result = memory.readQuadWord(left.data) and right
                            memory.writeQuad(left.data, result)
                        }
                    }
                }
                InstructionSet.OR -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a destination of size byte but instead found $left")
                        return@binaryOperation
                    }
                    when(right){
                        is DataType.Byte -> {
                            val result = memory.readByte(left.data) or right
                            memory.writeByte(left.data, result)
                        }
                        is DataType.Word -> {
                            val result = memory.readWord(left.data) or right
                            memory.writeWord(left.data, result)
                        }
                        is DataType.DoubleWord -> {
                            val result = memory.readDoubleWord(left.data) or right
                            memory.writeDouble(left.data, result)
                        }
                        is DataType.QuadWord -> {
                            val result = memory.readQuadWord(left.data) or right
                            memory.writeQuad(left.data, result)
                        }
                    }
                }
                InstructionSet.XOR -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a destination of size byte but instead found $left")
                        return@binaryOperation
                    }
                    when(right){
                        is DataType.Byte -> {
                            val result = memory.readByte(left.data) xor right
                            memory.writeByte(left.data, result)
                        }
                        is DataType.Word -> {
                            val result = memory.readWord(left.data) xor right
                            memory.writeWord(left.data, result)
                        }
                        is DataType.DoubleWord -> {
                            val result = memory.readDoubleWord(left.data) xor right
                            memory.writeDouble(left.data, result)
                        }
                        is DataType.QuadWord -> {
                            val result = memory.readQuadWord(left.data) xor right
                            memory.writeQuad(left.data, result)
                        }
                    }
                }
                InstructionSet.SHR -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a destination of size byte but instead found $left")
                        return@binaryOperation
                    }
                    when(right){
                        is DataType.Byte -> {
                            val result = memory.readByte(left.data) shr right
                            memory.writeByte(left.data, result)
                        }
                        is DataType.Word -> {
                            val result = memory.readWord(left.data) shr right
                            memory.writeWord(left.data, result)
                        }
                        is DataType.DoubleWord -> {
                            val result = memory.readDoubleWord(left.data) shr right
                            memory.writeDouble(left.data, result)
                        }
                        is DataType.QuadWord -> {
                            val result = memory.readQuadWord(left.data) shr right
                            memory.writeQuad(left.data, result)
                        }
                    }
                }
                InstructionSet.SHL -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a destination of size byte but instead found $left")
                        return@binaryOperation
                    }
                    when(right){
                        is DataType.Byte -> {
                            val result = memory.readByte(left.data) shl right
                            memory.writeByte(left.data, result)
                        }
                        is DataType.Word -> {
                            val result = memory.readWord(left.data) shl right
                            memory.writeWord(left.data, result)
                        }
                        is DataType.DoubleWord -> {
                            val result = memory.readDoubleWord(left.data) shl right
                            memory.writeDouble(left.data, result)
                        }
                        is DataType.QuadWord -> {
                            val result = memory.readQuadWord(left.data) shl right
                            memory.writeQuad(left.data, result)
                        }
                    }
                }
                InstructionSet.INV -> unaryOperator { operand ->
                    when(operand){
                        is DataType.Byte -> {
                            val data = memory.readByte(operand.data).inv()
                            memory.write(operand.data, data)
                        }
                        is DataType.Word -> {
                            val data = memory.readWord(operand.data2.data).inv()
                            memory.write(operand.data2.data, data)
                        }
                        is DataType.DoubleWord -> {
                            val data = memory.readDoubleWord(operand.data2.data2.data)
                            memory.write(operand.data2.data2.data, data)
                        }
                        is DataType.QuadWord -> {
                            val data = memory.readQuadWord(operand.data2.data2.data2.data)
                            memory.write(operand.data2.data2.data2.data, data)
                        }
                    }
                }
            }
        }
    }
}

