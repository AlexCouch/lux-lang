import InstructionSet
import arrow.core.None
import arrow.core.Some
import java.io.File

class Executable(private val instructions: IntArray): Iterator<Int>{
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
                append("0x${Integer.toHexString(it)}")
                if(idx < instructions.size - 1){
                    append(", ")
                }
            }
        }

    override fun hasNext(): Boolean = instructionPtr < instructions.size

    override fun next(): Int = instructions[instructionPtr++]

}

class Stack{
    private val stack: IntArray = IntArray(1024)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Stack

        if (!stack.contentEquals(other.stack)) return false

        return true
    }

    private var stackPtr = 0

    val top: Int
        get() = if(stackPtr == 0) 0 else stack[stackPtr - 1]

    override fun hashCode(): Int {
        return stack.contentHashCode()
    }

    fun push(data: Int){
        stack[stackPtr++] = data
    }

    fun pop(){
        stack[--stackPtr] = 0
    }

}

@ExperimentalStdlibApi
class VM(val binary: Executable){
    val memory = IntArray(1024)
    val stack = Stack()

    private fun move(){
        val dest = binary.next()
        val destInstr = InstructionSet.values().find { it.code == dest }
        val target = binary.next()
        val targetInstr = InstructionSet.values().find { it.code == target }
        when{
            targetInstr == null && destInstr == null -> memory[dest] = memory[target]
            targetInstr != null && destInstr == null ->
                when(targetInstr){
                    InstructionSet.TOP -> memory[dest] = InstructionSet.TOP.code
                    else -> {
                        println("Unknown memory address: $targetInstr")
                        return
                    }
                }
        }
    }

    private fun moveByte(){
        val dest = binary.next()
        val destInstr = InstructionSet.values().find { it.code == dest }
        val target = binary.next() and 0x00ff
        val targetInstr = InstructionSet.values().find { it.code == target }
        when{
            targetInstr == null && destInstr == null -> memory[dest] = target
            targetInstr != null && destInstr == null ->
                when(targetInstr) {
                    InstructionSet.TOP -> memory[dest] = InstructionSet.TOP.code
                    else -> {
                        println("Unknown memory address: $targetInstr")
                        return
                    }
                }
        }
    }

    private fun moveWord(){
        val dest = binary.next()
        val destInstr = InstructionSet.values().find { it.code == dest }
        val target = binary.next() and 0x00ffff
        val targetInstr = InstructionSet.values().find { it.code == target }
        when{
            targetInstr == null && destInstr == null -> memory[dest] = target
            targetInstr != null && destInstr == null ->
                when(targetInstr){
                    InstructionSet.TOP -> memory[dest] = InstructionSet.TOP.code
                    else -> {
                        println("Unknown memory address: $targetInstr")
                        return
                    }
                }
        }
    }

    private fun moveDoubleWord(){
        val dest = binary.next()
        val destInstr = InstructionSet.values().find { it.code == dest }
        val target = binary.next() and 0x00ffffff
        val targetInstr = InstructionSet.values().find { it.code == target }
        when{
            targetInstr == null && destInstr == null -> memory[dest] = target
            targetInstr != null && destInstr == null ->
                when(targetInstr){
                    InstructionSet.TOP -> memory[dest] = InstructionSet.TOP.code
                    else -> {
                        println("Unknown memory address: $targetInstr")
                        return
                    }
                }
        }
    }

    private fun moveLongWord(){
        val dest = binary.next()
        val destInstr = InstructionSet.values().find { it.code == dest }
        val target = binary.next()
        val targetInstr = InstructionSet.values().find { it.code == target }
        when{
            targetInstr == null && destInstr == null -> memory[dest] = target
            targetInstr != null && destInstr == null ->
                when(targetInstr){
                    InstructionSet.TOP -> memory[dest] = InstructionSet.TOP.code
                    else -> {
                        println("Unknown memory address: $targetInstr")
                        return
                    }
                }
        }
    }

    private fun push(){
        val data = binary.next()
        stack.push(data)
    }

    private fun pop(){
        stack.pop()
    }

    private fun jump(){
        val location = binary.next()
        val locInstr = InstructionSet.values().find { it.code == location }
        if(locInstr == null){
            binary.instructionPtr = location
        }
        when(locInstr){
            InstructionSet.TOP -> binary.instructionPtr = stack.top
        }
    }

    private fun add(){
        val left = binary.next()
        val leftInstr = InstructionSet.values().find { it.code == left }
        val leftValue = if(leftInstr != null){
            when(leftInstr){
                InstructionSet.TOP -> stack.top
                InstructionSet.REF -> {
                    val addr = binary.next()
                    memory[addr]
                }
                else -> left
            }
        }else{
            left
        }
        val right = binary.next()
        val rightInstr = InstructionSet.values().find { it.code == right }
        val rightValue = if(leftInstr != null){
            when(rightInstr){
                InstructionSet.TOP -> stack.top
                InstructionSet.REF -> {
                    val addr = binary.next()
                    memory[addr]
                }
                else -> right
            }
        }else{
            right
        }
        val sum = leftValue + rightValue
        stack.push(sum)
    }

    private fun sub(){
        val left = binary.next()
        val leftInstr = InstructionSet.values().find { it.code == left }
        val leftValue = if(leftInstr != null){
            when(leftInstr){
                InstructionSet.TOP -> stack.top
                InstructionSet.REF -> {
                    val addr = binary.next()
                    memory[addr]
                }
                else -> left
            }
        }else{
            left
        }
        val right = binary.next()
        val rightInstr = InstructionSet.values().find { it.code == right }
        val rightValue = if(leftInstr != null){
            when(rightInstr){
                InstructionSet.TOP -> stack.top
                InstructionSet.REF -> {
                    val addr = binary.next()
                    memory[addr]
                }
                else -> right
            }
        }else{
            right
        }

        val difference = leftValue - rightValue
        stack.push(difference)
    }

    private fun mul(){
        val left = binary.next()
        val leftInstr = InstructionSet.values().find { it.code == left }
        val leftValue = if(leftInstr != null){
            when(leftInstr){
                InstructionSet.TOP -> stack.top
                InstructionSet.REF -> {
                    val addr = binary.next()
                    memory[addr]
                }
                else -> left
            }
        }else{
            left
        }
        val right = binary.next()
        val rightInstr = InstructionSet.values().find { it.code == right }
        val rightValue = if(leftInstr != null){
            when(rightInstr){
                InstructionSet.TOP -> stack.top
                InstructionSet.REF -> {
                    val addr = binary.next()
                    memory[addr]
                }
                else -> right
            }
        }else{
            right
        }

        val product = leftValue * rightValue
        stack.push(product)
    }

    private fun div(){
        val left = binary.next()
        val leftInstr = InstructionSet.values().find { it.code == left }
        val leftValue = if(leftInstr != null){
            when(leftInstr){
                InstructionSet.TOP -> stack.top
                InstructionSet.REF -> {
                    val addr = binary.next()
                    memory[addr]
                }
                else -> left
            }
        }else{
            left
        }
        val right = binary.next()
        val rightInstr = InstructionSet.values().find { it.code == right }
        val rightValue = if(leftInstr != null){
            when(rightInstr){
                InstructionSet.TOP -> stack.top
                InstructionSet.REF -> {
                    val addr = binary.next()
                    memory[addr]
                }
                else -> right
            }
        }else{
            right
        }

        val quotient = leftValue / rightValue
        stack.push(quotient)
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
                InstructionSet.MOVE -> move()
                InstructionSet.MOVB -> moveByte()
                InstructionSet.MOVW -> moveWord()
                InstructionSet.MOVD -> moveDoubleWord()
                InstructionSet.MOVL -> moveLongWord()
                InstructionSet.PUSH -> push()
                InstructionSet.POP -> pop()
                InstructionSet.JMP -> jump()
                InstructionSet.ADD -> add()
                InstructionSet.SUB -> sub()
                InstructionSet.MUL -> mul()
                InstructionSet.DIV -> div()
            }
        }
    }
}

@ExperimentalStdlibApi
fun main(){
//    val file = File("asm/test.lasm")
//    if(!file.exists()){
//        println("asm/test.lasm doesn't exist!")
//        return
//    }
//    val asm = ASMLoader(file)
//    val exec = when(val result = asm.executable){
//        is Some -> result.t
//        is None -> return
//    }
//    println(exec)
    val exec = Executable(intArrayOf(
        InstructionSet.ADD.code,
        5,
        3,
        InstructionSet.MOVL.code,
        0,
        InstructionSet.TOP.code,
        InstructionSet.POP.code,
        InstructionSet.DIV.code,
        InstructionSet.REF.code,
        0,
        2,
        InstructionSet.MOVL.code,
        1,
        InstructionSet.TOP.code,
        InstructionSet.POP.code
    ))
    val vm = VM(exec)
    vm.run()
}