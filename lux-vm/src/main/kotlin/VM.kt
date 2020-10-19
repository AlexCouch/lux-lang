import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.left
import java.io.File


@ExperimentalUnsignedTypes
class Executable(private val instructions: Array<UByte>): Iterator<UByte>{
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

    override fun next(): UByte = instructions[instructionPtr++]

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
        file.writeBytes(instructions.toUByteArray().toByteArray())
    }

}

@ExperimentalUnsignedTypes
class Stack{
    private val stack: Array<UByte> = Array(1024){ 0u.toUByte() }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Stack

        if (!stack.contentEquals(other.stack)) return false

        return true
    }

    private var stackPtr = 0

    val top: UByte
        get() = if(stackPtr == 0) 0.toUByte() else stack[stackPtr - 1]

    override fun hashCode(): Int {
        return stack.contentHashCode()
    }

    fun push(data: DataType){
        when(data){
            is DataType.Byte -> {
                if(stack.isEmpty()){
                    stack[0] = data.data
                }else{
                    stack[stackPtr++] = data.data
                }
            }
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
        stack[--stackPtr] = 0.toUByte()
    }

}

@ExperimentalUnsignedTypes
class Memory{
    val memory = Array<UByte>(1024){ 0.toUByte() }

    fun write(dest: UByte, target: DataType){
        when(target){
            is DataType.Byte -> writeByte(dest, target)
            is DataType.SignedByte -> writeByte(dest, target.toByte())
            is DataType.Word -> writeWord(dest, target)
            is DataType.DoubleWord -> writeDouble(dest, target)
            is DataType.QuadWord -> writeQuad(dest, target)
        }
    }

    fun writeByte(dest: UByte, target: DataType.Byte){
        memory[dest.toInt()] = target.data
    }

    fun writeWord(dest: UByte, target: DataType.Word){
        memory[dest.toInt()] = target.data1.data
        memory[dest.toInt() + 1] = target.data2.data
    }

    fun writeDouble(dest: UByte, target: DataType.DoubleWord){
        memory[dest.toInt()] = target.data1.data1.data
        memory[dest.toInt()+1] = target.data1.data2.data
        memory[dest.toInt()+2] = target.data2.data1.data
        memory[dest.toInt()+3] = target.data2.data2.data
    }
    fun writeQuad(dest: UByte, target: DataType.QuadWord){
        memory[dest.toInt()] = target.data1.data1.data1.data
        memory[dest.toInt()+1] = target.data1.data1.data2.data
        memory[dest.toInt()+2] = target.data1.data2.data1.data
        memory[dest.toInt()+3] = target.data1.data2.data2.data
        memory[dest.toInt()+4] = target.data2.data1.data1.data
        memory[dest.toInt()+5] = target.data2.data1.data2.data
        memory[dest.toInt()+6] = target.data2.data2.data1.data
        memory[dest.toInt()+7] = target.data2.data2.data2.data
    }

    fun readByte(dest: UByte) = DataType.Byte(memory[dest.toInt()])
    fun readSignedByte(dest: UByte) = DataType.SignedByte(memory[dest.toInt()].toByte())
    fun readWord(dest: UByte) = DataType.Word(DataType.Byte(memory[dest.toInt()]), DataType.Byte(memory[dest.toInt()+1]))
    fun readDoubleWord(dest: UByte) = DataType.DoubleWord(
        DataType.Word(
            DataType.Byte(memory[dest.toInt()]),
            DataType.Byte(memory[dest.toInt()+1])
        ),
        DataType.Word(
            DataType.Byte(memory[dest.toInt()+2]),
            DataType.Byte(memory[dest.toInt()+3])
        )
    )

    fun readQuadWord(dest: UByte) = DataType.QuadWord(
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
@ExperimentalUnsignedTypes
class VM(val binary: Executable){
    val memory = Memory()
    val stack = Stack()

    private fun reference(operandPtr: Int, operands: ArrayList<DataType.Byte>): Tuple2<DataType, Int> {
        var localOperandPtr = operandPtr
        val next = operands[localOperandPtr++]
        val nextInstr = InstructionSet.values().find { it.code == next.data }
        return when{
            nextInstr != null -> {
                val n = operands[localOperandPtr++].data
                val nInstr = InstructionSet.values().find { n == it.code }
                if(nInstr != null){
                    when(nextInstr){
                        InstructionSet.BYTE -> Tuple2(memory.readByte(operands[localOperandPtr++].data), localOperandPtr)
                        InstructionSet.WORD -> Tuple2(memory.readWord(operands[localOperandPtr++].data), localOperandPtr)
                        InstructionSet.DWORD -> Tuple2(memory.readDoubleWord(operands[localOperandPtr++].data), localOperandPtr)
                        InstructionSet.QWORD -> Tuple2(memory.readQuadWord(operands[localOperandPtr++].data), localOperandPtr)
                        InstructionSet.TOP -> Tuple2(DataType.Byte(stack.top), localOperandPtr)
                        else -> Tuple2(memory.readByte(next.data), localOperandPtr)
                    }
                }else{
                    Tuple2(memory.readByte(n), localOperandPtr)
                }
            }
            else -> Tuple2(memory.readByte(next.data), localOperandPtr)
        }
    }

    private fun offset(left: DataType, operandPtr: Int, operands: ArrayList<DataType.Byte>): Either<Tuple2<DataType, Int>, String> {
        var ptr = operandPtr
        val next = operands[ptr++]
        val nextInst = InstructionSet.values().find { it.code == next.data }
        val value = if(nextInst != null){
            when(nextInst){
                InstructionSet.BYTE -> operands[ptr++]
                InstructionSet.WORD -> DataType.Word(operands[ptr++], operands[ptr++])
                InstructionSet.DWORD ->
                    DataType.DoubleWord(
                        DataType.Word(operands[ptr++], operands[ptr++]),
                        DataType.Word(operands[ptr++], operands[ptr++])
                    )
                InstructionSet.QWORD ->
                    DataType.QuadWord(
                        DataType.DoubleWord(
                            DataType.Word(operands[ptr++], operands[ptr++]),
                            DataType.Word(operands[ptr++], operands[ptr++])
                        ),
                        DataType.DoubleWord(
                            DataType.Word(operands[ptr++], operands[ptr++]),
                            DataType.Word(operands[ptr++], operands[ptr++])
                        )
                    )
                else -> next
            }
        }else{
            next
        }
        return Tuple2(when(val result = left.plus(value)){
            is Either.Left -> result.a
            is Either.Right -> return result
        }, ptr).left()
    }

    private fun noffset(left: DataType, operandPtr: Int, operands: ArrayList<DataType.Byte>): Either<Tuple2<DataType, Int>, String> {
        var ptr = operandPtr
        val next = operands[ptr++]
        val nextInst = InstructionSet.values().find { it.code == next.data }
        val value = if(nextInst != null){
            when(nextInst){
                InstructionSet.BYTE -> operands[ptr++]
                InstructionSet.WORD -> DataType.Word(operands[ptr++], operands[ptr++])
                InstructionSet.DWORD ->
                    DataType.DoubleWord(
                        DataType.Word(operands[ptr++], operands[ptr++]),
                        DataType.Word(operands[ptr++], operands[ptr++])
                    )
                InstructionSet.QWORD ->
                    DataType.QuadWord(
                        DataType.DoubleWord(
                            DataType.Word(operands[ptr++], operands[ptr++]),
                            DataType.Word(operands[ptr++], operands[ptr++])
                        ),
                        DataType.DoubleWord(
                            DataType.Word(operands[ptr++], operands[ptr++]),
                            DataType.Word(operands[ptr++], operands[ptr++])
                        )
                    )
                else -> next
            }
        }else{
            next
        }
        return Tuple2(when(val result = left.minus(value)){
            is Either.Left -> result.a
            is Either.Right -> return result
        }, ptr).left()
    }

    private fun unaryOperatorOrNone(block: (operand: DataType) -> Unit){
        val varOp = binary.nextByte()
        if(varOp.data != InstructionSet.ARGS.code){
            return
        }
        val numOperands = binary.nextByte()
        val operands = arrayListOf<DataType.Byte>()
        for(i in 1.toUByte() .. numOperands.data){
            operands += binary.nextByte()
        }
        var operandPtr = 0
        val operand = operands[operandPtr++]
        val operandInstr = InstructionSet.values().find { it.code == operand.data }
        if(operandInstr == InstructionSet.NOARGS){
            return
        }
        val operandValue = if(operandInstr != null){
            when(operandInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val (ref, ptr) = reference(operandPtr, operands)
                    operandPtr = ptr
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
                else -> operand
            }
        }else{
            operand
        }
        block(operandValue)
    }

    private fun unaryOperator(block: (operand: DataType) -> Unit){
        val varOp = binary.nextByte()
        if(varOp.data != InstructionSet.ARGS.code){
            println("Expected a number of args to parse but instead found $varOp")
            return
        }
        val numOperands = binary.nextByte()
        val operands = arrayListOf<DataType.Byte>()
        for(i in 1.toUByte() .. numOperands.data){
            operands += binary.nextByte()
        }
        var operandPtr = 0
        val operand = operands[operandPtr++]
        val operandInstr = InstructionSet.values().find { it.code == operand.data }
        val operandValue = if(operandInstr != null){
            when(operandInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val (ref, ptr) = reference(operandPtr, operands)
                    operandPtr = ptr
                    if(ref !is DataType.Byte){
                        println("Expected a destination of size byte but instead found size $ref")
                        return
                    }
                    ref
                }
                InstructionSet.BYTE -> {
                    val byte = operands[operandPtr++]
                    if(operands.size <= operandPtr+1){
                        byte
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(byte, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(byte, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> byte
                            }
                        } else {
                            byte
                        }
                    }
                }
                InstructionSet.WORD -> {
                    val word = DataType.Word(operands[operandPtr++], operands[operandPtr++])
                    val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                    if(peek != null){
                        when(peek){
                            InstructionSet.OFFSET -> {
                                val (data, ptr) = when(val result = offset(word, ++operandPtr, operands)){
                                    is Either.Left -> result.a
                                    is Either.Right -> {
                                        println(result.b)
                                        return
                                    }
                                }
                                operandPtr = ptr
                                data
                            }
                            InstructionSet.NOFFSET -> {
                                val (data, ptr) = when(val result = noffset(word, ++operandPtr, operands)){
                                    is Either.Left -> result.a
                                    is Either.Right -> {
                                        println(result.b)
                                        return
                                    }
                                }
                                operandPtr = ptr
                                data
                            }
                            else -> word
                        }
                    }else{
                        word
                    }
                }
                InstructionSet.DWORD -> {
                    val word = DataType.DoubleWord(
                        DataType.Word(operands[operandPtr++], operands[operandPtr++]),
                        DataType.Word(operands[operandPtr++], operands[operandPtr++])
                    )
                    if(operands.size <= operandPtr+1){
                        word
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(word, operandPtr++, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(word, operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> word
                            }
                        } else {
                            word
                        }
                    }
                }
                InstructionSet.QWORD -> {
                    val word = DataType.QuadWord(
                        DataType.DoubleWord(
                            DataType.Word(operands[operandPtr++], operands[operandPtr++]),
                            DataType.Word(operands[operandPtr++], operands[operandPtr++])
                        ),
                        DataType.DoubleWord(
                            DataType.Word(operands[operandPtr++], operands[operandPtr++]),
                            DataType.Word(operands[operandPtr++], operands[operandPtr++])
                        )
                    )
                    if(operands.size <= operandPtr+1){
                        word
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> word
                            }
                        } else {
                            word
                        }
                    }
                }
                else -> {
                    val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                    if(peek != null){
                        when(peek){
                            InstructionSet.OFFSET -> {
                                val (data, ptr) = when(val result = offset(operand, ++operandPtr, operands)){
                                    is Either.Left -> result.a
                                    is Either.Right -> {
                                        println(result.b)
                                        return
                                    }
                                }
                                operandPtr = ptr
                                data
                            }
                            InstructionSet.NOFFSET -> {
                                val (data, ptr) = when(val result = noffset(operand, ++operandPtr, operands)){
                                    is Either.Left -> result.a
                                    is Either.Right -> {
                                        println(result.b)
                                        return
                                    }
                                }
                                operandPtr = ptr
                                data
                            }
                            else -> operand
                        }
                    }else{
                        operand
                    }
                }
            }
        }else{
            val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
            if(peek != null){
                when(peek){
                    InstructionSet.OFFSET -> {
                        val (data, ptr) = when(val result = offset(operand, ++operandPtr, operands)){
                            is Either.Left -> result.a
                            is Either.Right -> {
                                println(result.b)
                                return
                            }
                        }
                        operandPtr = ptr
                        data
                    }
                    InstructionSet.NOFFSET -> {
                        val (data, ptr) = when(val result = noffset(operand, ++operandPtr, operands)){
                            is Either.Left -> result.a
                            is Either.Right -> {
                                println(result.b)
                                return
                            }
                        }
                        operandPtr = ptr
                        data
                    }
                    else -> operand
                }
            }else{
                operand
            }
        }
        block(operandValue)
    }

    fun binaryOperation(block: (left: DataType, right: DataType) -> Unit){
        val varOp = binary.nextByte()
        if(varOp.data != InstructionSet.ARGS.code){
            println("Expected a number of args to parse but instead found $varOp")
            return
        }
        val numOperands = binary.nextByte()
        val operands = arrayListOf<DataType.Byte>()
        for(i in 0.toUByte() until numOperands.data){
            operands += binary.nextByte()
        }
        var operandPtr = 0
        val left = operands[operandPtr++]
        val leftInstr = InstructionSet.values().find { it.code == left.data }
        val leftValue = if(leftInstr != null){
            when(leftInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val (ref, ptr) = reference(operandPtr, operands)
                    operandPtr = ptr
                    if(ref !is DataType.Byte){
                        println("Expected a destination of size byte but instead found size $ref")
                        return
                    }
                    ref
                }
                else -> {
                    val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                    if(peek != null){
                        when(peek){
                            InstructionSet.OFFSET -> {
                                val (data, ptr) = when(val result = offset(left, ++operandPtr, operands)){
                                    is Either.Left -> result.a
                                    is Either.Right -> {
                                        println(result.b)
                                        return
                                    }
                                }
                                operandPtr = ptr
                                data
                            }
                            InstructionSet.NOFFSET -> {
                                val (data, ptr) = when(val result = noffset(left, ++operandPtr, operands)){
                                    is Either.Left -> result.a
                                    is Either.Right -> {
                                        println(result.b)
                                        return
                                    }
                                }
                                operandPtr = ptr
                                data
                            }
                            else -> left
                        }
                    }else{
                        left
                    }
                }
            }
        }else{
            val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
            if(peek != null){
                when(peek){
                    InstructionSet.OFFSET -> {
                        val (data, ptr) = when(val result = offset(left, ++operandPtr, operands)){
                            is Either.Left -> result.a
                            is Either.Right -> {
                                println(result.b)
                                return
                            }
                        }
                        operandPtr = ptr
                        data
                    }
                    InstructionSet.NOFFSET -> {
                        val (data, ptr) = when(val result = noffset(left, ++operandPtr, operands)){
                            is Either.Left -> result.a
                            is Either.Right -> {
                                println(result.b)
                                return
                            }
                        }
                        operandPtr = ptr
                        data
                    }
                    else -> left
                }
            }else{
                left
            }
        }
        val right = operands[operandPtr++]
        val rightInstr = InstructionSet.values().find { it.code == right.data }
        val rightValue = if(rightInstr != null){
            when(rightInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val (ref, ptr) = reference(operandPtr, operands)
                    operandPtr = ptr
                    ref
                }
                InstructionSet.BYTE -> {
                    val byte = operands[operandPtr++]
                    if(operands.size <= operandPtr+1){
                        byte
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(byte, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(byte, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> byte
                            }
                        } else {
                            byte
                        }
                    }
                }
                InstructionSet.WORD -> {
                    val word = DataType.Word(operands[operandPtr++], operands[operandPtr++])
                    if(operands.size <= operandPtr+1){
                        word
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> word
                            }
                        } else {
                            word
                        }
                    }
                }
                InstructionSet.DWORD -> {
                    val word = DataType.DoubleWord(
                        DataType.Word(operands[operandPtr++], operands[operandPtr++]),
                        DataType.Word(operands[operandPtr++], operands[operandPtr++])
                    )
                    if(operands.size <= operandPtr+1){
                        word
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> word
                            }
                        } else {
                            word
                        }
                    }
                }
                InstructionSet.QWORD -> {
                    val word = DataType.QuadWord(
                        DataType.DoubleWord(
                            DataType.Word(operands[operandPtr++], operands[operandPtr++]),
                            DataType.Word(operands[operandPtr++], operands[operandPtr++])
                        ),
                        DataType.DoubleWord(
                            DataType.Word(operands[operandPtr++], operands[operandPtr++]),
                            DataType.Word(operands[operandPtr++], operands[operandPtr++])
                        )
                    )
                    if(operands.size <= operandPtr+1){
                        word
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> word
                            }
                        } else {
                            word
                        }
                    }
                }
                else -> right
            }
        }else{
            right
        }
        block(leftValue, rightValue)
    }

    fun trinaryOperation(block: (left: DataType, middle: DataType, right: DataType) -> Unit){
        val varOp = binary.nextByte()
        if(varOp.data != InstructionSet.ARGS.code){
            println("Expected a number of args to parse but instead found $varOp")
            return
        }
        var operandPtr = 0
        val numOperands = binary.nextByte()
        val operands = arrayListOf<DataType.Byte>()
        for(i in 0.toUByte() until numOperands.data){
            operands += binary.nextByte()
        }
        val left = operands[operandPtr++]
        val leftInstr = InstructionSet.values().find { it.code == left.data }
        val leftValue = if(leftInstr != null){
            when(leftInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val (ref, ptr) = reference(operandPtr, operands)
                    operandPtr = ptr
                    ref
                }
                else -> {
                    val peek = InstructionSet.values().find { it.code == operands[operandPtr+1].data }
                    if(peek != null){
                        when(peek){
                            InstructionSet.OFFSET -> {
                                val (data, ptr) = when(val result = offset(left, ++operandPtr, operands)){
                                    is Either.Left -> result.a
                                    is Either.Right -> {
                                        println(result.b)
                                        return
                                    }
                                }
                                operandPtr = ptr
                                data
                            }
                            InstructionSet.NOFFSET -> {
                                val (data, ptr) = when(val result = noffset(left, ++operandPtr, operands)){
                                    is Either.Left -> result.a
                                    is Either.Right -> {
                                        println(result.b)
                                        return
                                    }
                                }
                                operandPtr = ptr
                                data
                            }
                            else -> left
                        }
                    }else{
                        left
                    }
                }
            }
        }else{
            val peek = InstructionSet.values().find { it.code == operands[operandPtr+1].data }
            if(peek != null){
                when(peek){
                    InstructionSet.OFFSET -> {
                        val (data, ptr) = when(val result = offset(left, ++operandPtr, operands)){
                            is Either.Left -> result.a
                            is Either.Right -> {
                                println(result.b)
                                return
                            }
                        }
                        operandPtr = ptr
                        data
                    }
                    InstructionSet.NOFFSET -> {
                        val (data, ptr) = when(val result = noffset(left, ++operandPtr, operands)){
                            is Either.Left -> result.a
                            is Either.Right -> {
                                println(result.b)
                                return
                            }
                        }
                        operandPtr = ptr
                        data
                    }
                    else -> left
                }
            }else{
                left
            }
        }
        val middle = operands[operandPtr++]
        val middleInstr = InstructionSet.values().find { it.code == middle.data }
        val middleValue = if(middleInstr != null){
            when(middleInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val (ref, ptr) = reference(operandPtr, operands)
                    operandPtr = ptr
                    ref
                }
                InstructionSet.BYTE -> {
                    val byte = operands[operandPtr++]
                    if(operands.size <= operandPtr+1){
                        byte
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(byte, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(byte, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> byte
                            }
                        } else {
                            byte
                        }
                    }
                }
                InstructionSet.WORD -> {
                    val word = DataType.Word(operands[operandPtr++], operands[operandPtr++])
                    if(operands.size <= operandPtr+1){
                        word
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> word
                            }
                        } else {
                            word
                        }
                    }
                }
                InstructionSet.DWORD -> {
                    val word = DataType.DoubleWord(
                        DataType.Word(operands[operandPtr++], operands[operandPtr++]),
                        DataType.Word(operands[operandPtr++], operands[operandPtr++])
                    )
                    if(operands.size <= operandPtr+1){
                        word
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> word
                            }
                        } else {
                            word
                        }
                    }
                }
                InstructionSet.QWORD -> {
                    val word = DataType.QuadWord(
                        DataType.DoubleWord(
                            DataType.Word(operands[operandPtr++], operands[operandPtr++]),
                            DataType.Word(operands[operandPtr++], operands[operandPtr++])
                        ),
                        DataType.DoubleWord(
                            DataType.Word(operands[operandPtr++], operands[operandPtr++]),
                            DataType.Word(operands[operandPtr++], operands[operandPtr++])
                        )
                    )
                    if(operands.size <= operandPtr+1){
                        word
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(word, operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> word
                            }
                        } else {
                            word
                        }
                    }
                }
                else -> {
                    val peek = InstructionSet.values().find { it.code == operands[operandPtr+1].data }
                    if(peek != null){
                        when(peek){
                            InstructionSet.OFFSET -> {
                                val (data, ptr) = when(val result = offset(left, ++operandPtr, operands)){
                                    is Either.Left -> result.a
                                    is Either.Right -> {
                                        println(result.b)
                                        return
                                    }
                                }
                                operandPtr = ptr
                                data
                            }
                            InstructionSet.NOFFSET -> {
                                val (data, ptr) = when(val result = noffset(left, ++operandPtr, operands)){
                                    is Either.Left -> result.a
                                    is Either.Right -> {
                                        println(result.b)
                                        return
                                    }
                                }
                                operandPtr = ptr
                                data
                            }
                            else -> middle
                        }
                    }else{
                        middle
                    }
                }
            }
        }else{
            val peek = InstructionSet.values().find { it.code == operands[operandPtr+1].data }
            if(peek != null){
                when(peek){
                    InstructionSet.OFFSET -> {
                        val (data, ptr) = when(val result = offset(left, ++operandPtr, operands)){
                            is Either.Left -> result.a
                            is Either.Right -> {
                                println(result.b)
                                return
                            }
                        }
                        operandPtr = ptr
                        data
                    }
                    InstructionSet.NOFFSET -> {
                        val (data, ptr) = when(val result = noffset(left, ++operandPtr, operands)){
                            is Either.Left -> result.a
                            is Either.Right -> {
                                println(result.b)
                                return
                            }
                        }
                        operandPtr = ptr
                        data
                    }
                    else -> middle
                }
            }else{
                middle
            }
        }
        val right = operands[operandPtr++]
        val rightInstr = InstructionSet.values().find { it.code == right.data }
        val rightValue = if(rightInstr != null){
            when(rightInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val (ref, ptr) = reference(operandPtr, operands)
                    operandPtr = ptr
                    ref
                }
                InstructionSet.BYTE -> {
                    val byte = operands[operandPtr++]
                    if(operands.size <= operandPtr+1){
                        byte
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr + 1].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(byte, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(byte, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> byte
                            }
                        } else {
                            byte
                        }
                    }
                }
                InstructionSet.WORD -> {
                    val word = binary.nextWord()
                    if(operands.size <= operandPtr+1){
                        word
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr + 1].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> word
                            }
                        } else {
                            word
                        }
                    }
                }
                InstructionSet.DWORD -> {
                    val word = binary.nextDoubleWord()
                    if(operands.size <= operandPtr+1){
                        word
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr + 1].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> word
                            }
                        } else {
                            word
                        }
                    }
                }
                InstructionSet.QWORD -> {
                    val word = binary.nextQuadWord()
                    if(operands.size <= operandPtr+1){
                        word
                    }else {
                        val peek = InstructionSet.values().find { it.code == operands[operandPtr + 1].data }
                        if (peek != null) {
                            when (peek) {
                                InstructionSet.OFFSET -> {
                                    val (data, ptr) = when (val result = offset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                InstructionSet.NOFFSET -> {
                                    val (data, ptr) = when (val result = noffset(word, ++operandPtr, operands)) {
                                        is Either.Left -> result.a
                                        is Either.Right -> {
                                            println(result.b)
                                            return
                                        }
                                    }
                                    operandPtr = ptr
                                    data
                                }
                                else -> word
                            }
                        } else {
                            word
                        }
                    }
                }
                else -> {
                    val peek = InstructionSet.values().find { it.code == operands[operandPtr+1].data }
                    if(peek != null){
                        when(peek){
                            InstructionSet.OFFSET -> {
                                val (data, ptr) = when(val result = offset(left, ++operandPtr, operands)){
                                    is Either.Left -> result.a
                                    is Either.Right -> {
                                        println(result.b)
                                        return
                                    }
                                }
                                operandPtr = ptr
                                data
                            }
                            InstructionSet.NOFFSET -> {
                                val (data, ptr) = when(val result = noffset(left, ++operandPtr, operands)){
                                    is Either.Left -> result.a
                                    is Either.Right -> {
                                        println(result.b)
                                        return
                                    }
                                }
                                operandPtr = ptr
                                data
                            }
                            else -> right
                        }
                    }else{
                        right
                    }
                }
            }
        }else{
            val peek = InstructionSet.values().find { it.code == operands[operandPtr+1].data }
            if(peek != null){
                when(peek){
                    InstructionSet.OFFSET -> {
                        val (data, ptr) = when(val result = offset(left, ++operandPtr, operands)){
                            is Either.Left -> result.a
                            is Either.Right -> {
                                println(result.b)
                                return
                            }
                        }
                        operandPtr = ptr
                        data
                    }
                    InstructionSet.NOFFSET -> {
                        val (data, ptr) = when(val result = noffset(left, ++operandPtr, operands)){
                            is Either.Left -> result.a
                            is Either.Right -> {
                                println(result.b)
                                return
                            }
                        }
                        operandPtr = ptr
                        data
                    }
                    else -> right
                }
            }else{
                right
            }
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
                    val rData = if(right !is DataType.Byte){
                        right.toByte()
                    }else{
                        right
                    }
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    memory.writeByte(left.data, rData)
                }
                InstructionSet.MOVW -> binaryOperation{ left, right ->
                    val rData = if(right !is DataType.Word){
                        right.toWord()
                    }else{
                        right
                    }
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    memory.writeWord(left.data, rData)
                }
                InstructionSet.MOVD -> binaryOperation{ left, right ->
                    val rData = if(right !is DataType.DoubleWord){
                        right.toDouble()
                    }else{
                        right
                    }
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    memory.writeDouble(left.data, rData)
                }
                InstructionSet.MOVQ -> binaryOperation{ left, right ->
                    val rData = if(right !is DataType.QuadWord){
                        right.toQuad()
                    }else{
                        right
                    }
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    memory.writeQuad(left.data, rData)
                }
                InstructionSet.PUSH -> unaryOperator{ operand ->
                    stack.push(operand)
                }
                InstructionSet.POP -> unaryOperatorOrNone { operand ->
                    if(operand !is DataType.Byte){
                        println("Expected operand to be of size data but was instead $operand")
                        return@unaryOperatorOrNone
                    }
                    memory.write(operand.data, DataType.Byte(stack.top))
                    stack.pop()
                }
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
                    val sum = when(right){
                        is DataType.Byte -> memory.readByte(left.data).plus(right)
                        is DataType.SignedByte -> memory.readSignedByte(left.data).plus(right)
                        is DataType.Word -> memory.readWord(left.data).plus(right)
                        is DataType.DoubleWord -> memory.readDoubleWord(left.data).plus(right)
                        is DataType.QuadWord -> memory.readQuadWord(left.data).plus(right)
                    }
                    when(sum){
                        is Either.Left -> memory.write(left.data, sum.a)
                        is Either.Right -> {
                            println(sum.b)
                            return@binaryOperation
                        }
                    }
                }
                InstructionSet.SADD -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    val sum = when(right){
                        is DataType.Byte -> memory.readSignedByte(left.data).plus(right)
                        is DataType.SignedByte -> memory.readSignedByte(left.data).plus(right)
                        is DataType.Word -> memory.readWord(left.data).plus(right)
                        is DataType.DoubleWord -> memory.readDoubleWord(left.data).plus(right)
                        is DataType.QuadWord -> memory.readQuadWord(left.data).plus(right)
                    }
                    when(sum){
                        is Either.Left -> memory.write(left.data, sum.a)
                        is Either.Right -> {
                            println(sum.b)
                            return@binaryOperation
                        }
                    }
                }
                InstructionSet.SUB -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    val diff = when(right){
                        is DataType.Byte ->  memory.readByte(left.data).minus(right)
                        is DataType.SignedByte ->  memory.readSignedByte(left.data).minus(right)
                        is DataType.Word -> memory.readWord(left.data).minus(right)
                        is DataType.DoubleWord -> memory.readDoubleWord(left.data).minus(right)
                        is DataType.QuadWord -> memory.readQuadWord(left.data).minus(right)
                    }
                    when(diff){
                        is Either.Left -> memory.write(left.data, diff.a)
                        is Either.Right -> {
                            println(diff.b)
                            return@binaryOperation
                        }
                    }
                }
                InstructionSet.SSUB -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    val diff = when(right){
                        is DataType.Byte ->  memory.readSignedByte(left.data).minus(right)
                        is DataType.SignedByte ->  memory.readSignedByte(left.data).minus(right)
                        is DataType.Word -> memory.readWord(left.data).minus(right)
                        is DataType.DoubleWord -> memory.readDoubleWord(left.data).minus(right)
                        is DataType.QuadWord -> memory.readQuadWord(left.data).minus(right)
                    }
                    when(diff){
                        is Either.Left -> memory.write(left.data, diff.a)
                        is Either.Right -> {
                            println(diff.b)
                            return@binaryOperation
                        }
                    }
                }
                InstructionSet.MUL -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    val product = when(right){
                        is DataType.Byte -> memory.readByte(left.data).times(right)
                        is DataType.SignedByte -> memory.readSignedByte(left.data).times(right)
                        is DataType.Word -> memory.readWord(left.data).times(right)
                        is DataType.DoubleWord -> memory.readDoubleWord(left.data).times(right)
                        is DataType.QuadWord -> memory.readQuadWord(left.data).times(right)
                    }
                    when(product){
                        is Either.Left -> memory.write(left.data, product.a)
                        is Either.Right -> {
                            println(product.b)
                            return@binaryOperation
                        }
                    }
                }
                InstructionSet.SMUL -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    val product = when(right){
                        is DataType.Byte -> memory.readSignedByte(left.data).times(right)
                        is DataType.SignedByte -> memory.readSignedByte(left.data).times(right)
                        is DataType.Word -> memory.readWord(left.data).times(right)
                        is DataType.DoubleWord -> memory.readDoubleWord(left.data).times(right)
                        is DataType.QuadWord -> memory.readQuadWord(left.data).times(right)
                    }
                    when(product){
                        is Either.Left -> memory.write(left.data, product.a)
                        is Either.Right -> {
                            println(product.b)
                            return@binaryOperation
                        }
                    }
                }
                InstructionSet.DIV -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    val quotient = when(right){
                        is DataType.Byte ->  memory.readByte(left.data).div(right)
                        is DataType.SignedByte ->  memory.readSignedByte(left.data).div(right)
                        is DataType.Word -> memory.readWord(left.data).minus(right)
                        is DataType.DoubleWord -> memory.readDoubleWord(left.data).div(right)
                        is DataType.QuadWord -> memory.readQuadWord(left.data).div(right)
                    }
                    when(quotient){
                        is Either.Left -> memory.write(left.data, quotient.a)
                        is Either.Right -> {
                            println(quotient.b)
                            return@binaryOperation
                        }
                    }
                }
                InstructionSet.SDIV -> binaryOperation{ left, right ->
                    if(left !is DataType.Byte){
                        println("Expected a data type of byte for left operand but instead got $right")
                        return@binaryOperation
                    }
                    val quotient = when(right){
                        is DataType.Byte ->  memory.readSignedByte(left.data).div(right)
                        is DataType.SignedByte ->  memory.readSignedByte(left.data).div(right)
                        is DataType.Word -> memory.readWord(left.data).minus(right)
                        is DataType.DoubleWord -> memory.readDoubleWord(left.data).div(right)
                        is DataType.QuadWord -> memory.readQuadWord(left.data).div(right)
                    }
                    when(quotient){
                        is Either.Left -> memory.write(left.data, quotient.a)
                        is Either.Right -> {
                            println(quotient.b)
                            return@binaryOperation
                        }
                    }
                }
                InstructionSet.LE -> binaryOperation{ left, right ->
                    val cmp = left <= right
                    stack.push(DataType.Byte(if(cmp) 1.toUByte() else 0.toUByte()))
                }
                InstructionSet.LT -> binaryOperation{ left, right ->
                    val cmp = left < right
                    stack.push(DataType.Byte(if(cmp) 1.toUByte() else 0.toUByte()))
                }
                InstructionSet.GE -> binaryOperation{ left, right ->
                    val cmp = left >= right
                    stack.push(DataType.Byte(if(cmp) 1.toUByte() else 0.toUByte()))
                }
                InstructionSet.GT -> binaryOperation{ left, right ->
                    val cmp = left > right
                    stack.push(DataType.Byte(if(cmp) 1.toUByte() else 0.toUByte()))
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

