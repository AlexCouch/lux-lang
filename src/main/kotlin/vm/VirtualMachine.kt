package vm

import TokenPos
import arrow.core.*
import bc.*

sealed class VMObject<T>(open val data: T){
    data class VMRef(
        override val data: Int
    ): VMObject<Int>(data)
    data class VMConstant<T>(
        override val data: T
    ): VMObject<T>(
        data
    )
    data class VMName(
        override val data: String
    ): VMObject<String>(
        data
    )
}

class VirtualMachine(val instructions: List<Byte>, val store: Store){
    val heap = arrayListOf<VMObject<*>>()
    val stack = arrayListOf<VMObject<*>>()
    private var insPtr = 0

    fun pushName(name: String): Option<Error>{
        stack += VMObject.VMName(name)
        return none()
    }

    fun <T> pushConstant(constant: T): Option<Error>{
        stack += VMObject.VMConstant(constant)
        return none()
    }

    fun <T> pushConstantHeap(constant: T): Option<Error>{
        heap += VMObject.VMConstant(constant)
        return none()
    }

    fun read(index: Int): Option<Error> {
        val name = store.names[index]
        val namei = stack.indexOfFirst { it is VMObject.VMName && it.data == name }
        if(namei == -1){
            return Error("Could not find name object of index $index on stack").some()
        }
        val nameObj = stack.removeAt(namei)
        val value = stack.removeAt(namei-1)
        stack += value
        stack += nameObj
        return none()
    }

    fun pushHeap(): Option<Error>{
        val next = instructions[++insPtr]
        if(next < Bytecode.values().size){
            when(Bytecode.values()[next.toInt()]){
                Bytecode.CONSTANT -> {
                    val operand = instructions[++insPtr]
                    val const = store.constants[operand.toInt()]
                    when(val result = pushConstantHeap(const.value)){
                        is Some -> return result
                        is None -> {}
                    }
                }
                Bytecode.PUSH_NAME -> {
                    val operand = instructions[++insPtr]
                    val namei = Operand.Integer(operand.toInt())
                    val name = store.names[namei.int]
                    when(val result = pushName(name)){
                        is Some -> return result
                        is None -> {}
                    }
                }
                Bytecode.READ -> {
                    val operand = instructions[++insPtr].toInt()
                    when(val result = read(operand)){
                        is Some -> return result
                        is None -> {}
                    }
                }
                Bytecode.REF -> {
                    val heapi = heap.lastIndex
                    stack += VMObject.VMRef(heapi)
                }
                Bytecode.ADD -> when(val result = binaryAddHeap()){
                    is Some -> return result
                    is None -> {}
                }
                Bytecode.SUB -> when(val result = binarySubHeap()){
                    is Some -> return result
                    is None -> {}
                }
                Bytecode.MUL -> when(val result = binaryMulHeap()){
                    is Some -> return result
                    is None -> {}
                }
                Bytecode.DIV -> when(val result = binaryDivHeap()){
                    is Some -> return result
                    is None -> {}
                }
            }
        }
        return none()
    }

    fun binaryAdd(): Option<Error>{
        val left = stack.removeAt(stack.lastIndex - 1)
        if(left is VMObject.VMConstant){
            if(left.data !is Int){
                return Error("Binary 'add' is not permitted for non-integer data types right now!").some()
            }
        }
        val right = stack.removeAt(stack.lastIndex)
        if(right is VMObject.VMConstant){
            if(right.data !is Int){
                return Error("Binary 'add' is not permitted for non-integer data types right now!").some()
            }
        }
        val leftData = left.data as Int
        val rightData = right.data as Int
        val sum = leftData + rightData
        stack += VMObject.VMConstant(sum)
        return none()
    }

    fun binaryAddHeap(): Option<Error>{
        val right = when(val right = stack[stack.lastIndex]){
            is VMObject.VMName -> if(stack[stack.lastIndex - 1] is VMObject.VMConstant){
                if(stack[stack.lastIndex - 1].data is Int){
                    stack.removeAt(stack.lastIndex)
                    stack.removeAt(stack.lastIndex - 1)
                }else{
                    return Error("Add operation not permitted for $right").some()
                }
            }else{
                return Error("Add operation not permitted for $right").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Add operation not permitted for $right").some()
        }
        val left = when(val left = stack[stack.lastIndex]){
            is VMObject.VMName -> if(stack[stack.lastIndex - 1] is VMObject.VMConstant){
                if(stack[stack.lastIndex - 1].data is Int){
                    stack.removeAt(stack.lastIndex)
                    stack.removeAt(stack.lastIndex - 1)
                }else{
                    return Error("Add operation not permitted for $left").some()
                }
            }else{
                return Error("Add operation not permitted for $left").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Add operation not permitted for $left").some()
        }
        val leftData = left.data as Int
        val rightData = right.data as Int
        val sum = leftData + rightData
        heap += VMObject.VMConstant(sum)
        return none()
    }

    fun binarySub(): Option<Error>{
        val right = when(val right = stack[stack.lastIndex]){
            is VMObject.VMName -> if(stack[stack.lastIndex - 1] is VMObject.VMConstant){
                if(stack[stack.lastIndex - 1].data is Int){
                    stack.removeAt(stack.lastIndex)
                    stack.removeAt(stack.lastIndex - 1)
                }else{
                    return Error("Sub operation not permitted for $right").some()
                }
            }else{
                return Error("Sub operation not permitted for $right").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Sub operation not permitted for $right").some()
        }
        val left = when(val left = stack[stack.lastIndex]){
            is VMObject.VMName -> if(stack[stack.lastIndex - 1] is VMObject.VMConstant){
                if(stack[stack.lastIndex - 1].data is Int){
                    stack.removeAt(stack.lastIndex)
                    stack.removeAt(stack.lastIndex - 1)
                }else{
                    return Error("Sub operation not permitted for $left").some()
                }
            }else{
                return Error("Sub operation not permitted for $left").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Sub operation not permitted for $left").some()
        }
        val leftData = left.data as Int
        val rightData = right.data as Int
        val sum = leftData - rightData
        stack += VMObject.VMConstant(sum)
        return none()
    }

    fun binarySubHeap(): Option<Error>{
        val right = when(val right = stack[stack.lastIndex]){
            is VMObject.VMName -> if(stack[stack.lastIndex - 1] is VMObject.VMConstant){
                if(stack[stack.lastIndex - 1].data is Int){
                    stack.removeAt(stack.lastIndex)
                    stack.removeAt(stack.lastIndex - 1)
                }else{
                    return Error("Sub operation not permitted for $right").some()
                }
            }else{
                return Error("Sub operation not permitted for $right").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Sub operation not permitted for $right").some()
        }
        val left = when(val left = stack[stack.lastIndex]){
            is VMObject.VMName -> if(stack[stack.lastIndex - 1] is VMObject.VMConstant){
                if(stack[stack.lastIndex - 1].data is Int){
                    stack.removeAt(stack.lastIndex)
                    stack.removeAt(stack.lastIndex - 1)
                }else{
                    return Error("Sub operation not permitted for $left").some()
                }
            }else{
                return Error("Sub operation not permitted for $left").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Sub operation not permitted for $left").some()
        }
        val leftData = left.data as Int
        val rightData = right.data as Int
        val sum = leftData - rightData
        heap += VMObject.VMConstant(sum)
        return none()
    }

    fun binaryMul(): Option<Error>{
        val right = when(val right = stack[stack.lastIndex]){
            is VMObject.VMName -> if(stack[stack.lastIndex - 1] is VMObject.VMConstant){
                if(stack[stack.lastIndex - 1].data is Int){
                    stack.removeAt(stack.lastIndex)
                    stack.removeAt(stack.lastIndex - 1)
                }else{
                    return Error("Mul operation not permitted for $right").some()
                }
            }else{
                return Error("Mul operation not permitted for $right").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Mul operation not permitted for $right").some()
        }
        val left = when(val left = stack[stack.lastIndex]){
            is VMObject.VMName -> if(stack[stack.lastIndex - 1] is VMObject.VMConstant){
                if(stack[stack.lastIndex - 1].data is Int){
                    stack.removeAt(stack.lastIndex)
                    stack.removeAt(stack.lastIndex - 1)
                }else{
                    return Error("Mul operation not permitted for $left").some()
                }
            }else{
                return Error("Mul operation not permitted for $left").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Mul operation not permitted for $left").some()
        }
        val leftData = left.data as Int
        val rightData = right.data as Int
        val sum = leftData * rightData
        stack += VMObject.VMConstant(sum)
        return none()
    }

    fun binaryMulHeap(): Option<Error>{
        val right = when(val right = stack[stack.lastIndex]){
            is VMObject.VMName -> if(stack[stack.lastIndex - 1] is VMObject.VMConstant){
                if(stack[stack.lastIndex - 1].data is Int){
                    stack.removeAt(stack.lastIndex)
                    stack.removeAt(stack.lastIndex - 1)
                }else{
                    return Error("Mul operation not permitted for $right").some()
                }
            }else{
                return Error("Mul operation not permitted for $right").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Mul operation not permitted for $right").some()
        }
        val left = when(val left = stack[stack.lastIndex]){
            is VMObject.VMName -> if(stack[stack.lastIndex - 1] is VMObject.VMConstant){
                if(stack[stack.lastIndex - 1].data is Int){
                    stack.removeAt(stack.lastIndex)
                    stack.removeAt(stack.lastIndex - 1)
                }else{
                    return Error("Mul operation not permitted for $left").some()
                }
            }else{
                return Error("Mul operation not permitted for $left").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Mul operation not permitted for $left").some()
        }
        val leftData = left.data as Int
        val rightData = right.data as Int
        val sum = leftData * rightData
        heap += VMObject.VMConstant(sum)
        return none()
    }

    fun binaryDiv(): Option<Error>{
        val right = when(val right = stack[stack.lastIndex]){
            is VMObject.VMName -> when(stack[stack.lastIndex - 1]){
                is VMObject.VMConstant ->{
                    if(stack[stack.lastIndex - 1].data is Int){
                        stack.removeAt(stack.lastIndex)
                        stack.removeAt(stack.lastIndex)
                    }else{
                        return Error("Div operation not permitted for $right").some()
                    }
                }
                is VMObject.VMRef -> {
                    stack.removeAt(stack.lastIndex)
                    val ref = stack.removeAt(stack.lastIndex)
                    if(ref.data !is Int){
                        return Error("Expected REF data to be integer, but was intead ${ref.data!!::class.java}").some()
                    }
                    heap[ref.data as Int]
                }
                else -> return Error("Div operation not permitted for $right").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Div operation not permitted for $right").some()
        }
        val left = when(val left = stack[stack.lastIndex]){
            is VMObject.VMName -> when(stack[stack.lastIndex - 1]){
                is VMObject.VMConstant ->{
                    if(stack[stack.lastIndex - 1].data is Int){
                        stack.removeAt(stack.lastIndex)
                        stack.removeAt(stack.lastIndex)
                    }else{
                        return Error("Div operation not permitted for $right").some()
                    }
                }
                is VMObject.VMRef -> {
                    stack.removeAt(stack.lastIndex)
                    val ref = stack.removeAt(stack.lastIndex)
                    if(ref.data !is Int){
                        return Error("Expected REF data to be integer, but was intead ${ref.data!!::class.java}").some()
                    }
                    heap[ref.data as Int]
                }
                else -> return Error("Div operation not permitted for $right").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Div operation not permitted for $left").some()
        }
        val leftData = left.data as Int
        val rightData = right.data as Int
        val sum = leftData / rightData
        stack += VMObject.VMConstant(sum)
        return none()
    }

    fun binaryDivHeap(): Option<Error>{
        val right = when(val right = stack[stack.lastIndex]){
            is VMObject.VMName -> if(stack[stack.lastIndex - 1] is VMObject.VMConstant){
                if(stack[stack.lastIndex - 1].data is Int){
                    stack.removeAt(stack.lastIndex)
                    stack.removeAt(stack.lastIndex - 1)
                }else{
                    return Error("Div operation not permitted for $right").some()
                }
            }else{
                return Error("Div operation not permitted for $right").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Div operation not permitted for $right").some()
        }
        val left = when(val left = stack[stack.lastIndex]){
            is VMObject.VMName -> if(stack[stack.lastIndex - 1] is VMObject.VMConstant){
                if(stack[stack.lastIndex - 1].data is Int){
                    stack.removeAt(stack.lastIndex)
                    stack.removeAt(stack.lastIndex - 1)
                }else{
                    return Error("Div operation not permitted for $left").some()
                }
            }else{
                return Error("Div operation not permitted for $left").some()
            }
            is VMObject.VMConstant -> stack.removeAt(stack.lastIndex)
            else -> return Error("Div operation not permitted for $left").some()
        }
        val leftData = left.data as Int
        val rightData = right.data as Int
        val sum = leftData / rightData
        heap += VMObject.VMConstant(sum)
        return none()
    }

    fun run(): Option<Error>{
        while(insPtr in instructions.indices){
            val next = instructions[insPtr]
            if(next < Bytecode.values().size){
                when(Bytecode.values()[next.toInt()]) {
                    Bytecode.PUSH_NAME -> {
                        val operand = instructions[++insPtr]
                        val namei = Operand.Integer(operand.toInt())
                        val name = store.names[namei.int]
                        when(val result = pushName(name)){
                            is Some -> return result
                            is None -> {}
                        }
                    }
                    Bytecode.CONSTANT -> {
                        val operand = instructions[++insPtr]
                        val namei = Operand.Integer(operand.toInt())
                        val name = store.constants[namei.int]
                        when(val result = pushConstant(name.value)){
                            is Some -> return result
                            is None -> {}
                        }
                    }
                    Bytecode.HEAP -> when(val result = pushHeap()){
                        is Some -> return result
                        is None -> {}
                    }
                    Bytecode.REF -> {
                        val heapi = heap.lastIndex
                        stack += VMObject.VMRef(heapi)
                    }
                    Bytecode.READ -> {
                        val operand = instructions[++insPtr].toInt()
                        when(val result = read(operand)){
                            is Some -> return result
                            is None -> {}
                        }
                    }
                    Bytecode.ADD -> when(val result = binaryAdd()){
                        is Some -> return result
                        is None -> {}
                    }
                    Bytecode.SUB -> when(val result = binarySub()){
                        is Some -> return result
                        is None -> {}
                    }
                    Bytecode.MUL -> when(val result = binaryMul()){
                        is Some -> return result
                        is None -> {}
                    }
                    Bytecode.DIV -> when(val result = binaryDiv()){
                        is Some -> return result
                        is None -> {}
                    }
                }
            }
            insPtr++
        }
        return none()
    }
}