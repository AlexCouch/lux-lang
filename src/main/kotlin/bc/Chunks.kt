package bc

import TokenPos
import buildPrettyString

data class Chunk(
    val bytes: List<Byte>,
    val subchunks: List<Chunk>,
    val startPos: TokenPos,
    val endPos: TokenPos
){
    @ExperimentalStdlibApi
    fun toBytestream(): List<Byte> =
        buildList{
            addAll(bytes)
            subchunks.forEach {
                addAll(it.toBytestream())
            }
        }

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            var bcIdx = -1
            appendWithNewLine("${startPos.pos.line}:${startPos.pos.col};${startPos.offset}")
            if(bytes.isNotEmpty()){
                spaced(2)
                indent {
                    val iter = bytes.iterator()
                    while(iter.hasNext()){
                        val it = iter.next()
                        bcIdx++
                        val int = it.toInt()
                        if(int < Bytecode.values().size){
                            val bc = Bytecode.values()[int]
                            append(bcIdx)
                            append(bc.name)
                            when(bc){
                                Bytecode.CONSTANT -> {
                                    val operand = iter.next()
                                    bcIdx++
                                    indent {
                                        append(operand.toInt())
                                    }
                                }
                                Bytecode.HEAP -> {}
                                Bytecode.PUSH_NAME -> {
                                    val operand = iter.next()
                                    bcIdx++
                                    indent {
                                        append(operand.toInt())
                                    }
                                }
                                Bytecode.ADD -> {}
                                Bytecode.SUB -> {}
                                Bytecode.MUL -> {}
                                Bytecode.DIV -> {}
                                Bytecode.REF -> {
                                    val operand = iter.next()
                                    bcIdx++
                                    indentN(3){
                                        append(operand.toInt())
                                    }
                                }
                                Bytecode.READ -> {
                                    val operand = iter.next()
                                    bcIdx++
                                    indentN(2){
                                        append(operand.toInt())
                                    }
                                }
                            }
                        }
                        appendWithNewLine("")
                    }
                }
            }
            val subchunksIter = subchunks.iterator()
            while(subchunksIter.hasNext()){
                val next = subchunksIter.next()
                bcIdx++
                appendWithNewLine(next.toString())
            }
        }
}

class BytecodeChunkBuilder(private val startPos: TokenPos, private val endPos: TokenPos){
    internal val bytecode = arrayListOf<Byte>()
    internal val subchunks = arrayListOf<Chunk>()

    fun append(opcode: Bytecode, block: OpcodeBuilder.()->Unit = {}){
        val opcodeBuilder = OpcodeBuilder(opcode.ordinal.toByte())
        opcodeBuilder.block()
        bytecode += opcodeBuilder.bytecode
    }

    fun appendSubchunk(chunk: Chunk){
        subchunks += chunk
    }

    fun appendChild(child: List<Byte>){
        bytecode += child
    }

    @ExperimentalStdlibApi
    fun build() = Chunk(bytecode, subchunks, startPos, endPos).toBytestream()
}

class OpcodeBuilder(opcode: Byte){
    internal val bytecode = arrayListOf(opcode)

    @ExperimentalStdlibApi
    fun append(operand: Operand){
        when(operand){
            is Operand.String -> bytecode.addAll(operand.str.encodeToByteArray().toList())
            is Operand.Integer -> bytecode += operand.int.toByte()
            else -> return
        }
    }
}

@ExperimentalStdlibApi
inline fun buildChunk(startPos: TokenPos, endPos: TokenPos, block: BytecodeChunkBuilder.() -> Unit): List<Byte> {
    val builder = BytecodeChunkBuilder(startPos, endPos)
    builder.block()
    return builder.build()
}