package bc

import TokenPos
import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.left
import arrow.core.right
import arrow.optics.extensions.list.cons.cons
import buildPrettyString
import errors.SourceAnnotation
import errors.buildSourceAnnotation
import ir.IRElement
import ir.IRStatement
import ir.declarations.*
import ir.declarations.expressions.*
import ir.visitors.IRElementVisitor

data class Instruction(
    val opcode: Bytecode,
    val opands: ArrayList<Operand>,
    val startPos: TokenPos,
    val endPos: TokenPos
)

sealed class Operand{
    data class String(val str: kotlin.String): Operand()
    data class Integer(val int: Int): Operand()
    object None: Operand()
}

data class Instructions(val bytecode: ArrayList<Instruction>){
    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString{
            bytecode.forEach {ins ->
                append("${ins.startPos.pos.line}:${ins.startPos.pos.col}")
                spaced(2){
                    indent {
                        append("${ins.opcode}")
                        ins.opands.withIndex().forEach { (idx, it) ->
                            when(it){
                                is Operand.String -> append("\"${it.str}\"")
                                is Operand.Integer -> append("${it.int}")
                                is Operand.None -> append("")
                            }
                            if(idx in 0 until ins.opands.size - 1){
                                append(", ")
                            }
                        }
                    }
                }
                appendWithNewLine("")
            }
        }
}

class BytecodeGenerator(val input: String): IRElementVisitor<List<Byte>, Store>{
    @ExperimentalStdlibApi
    override fun visitElement(element: IRElement, data: Store): Either<List<Byte>, SourceAnnotation> =
        when(element){
            is IRStatement -> visitStatement(element, data)
            else -> buildSourceAnnotation {
                message = "Failed to convert expression to bytecode"
                errorLine {
                    message = "Expected IRStatement but instead found $element"
                    start = element.startPos
                    end = element.endPos
                }
                sourceOrigin {
                    source = input
                    start = element.startPos
                    end = element.endPos
                }
            }.right()
        }

    @ExperimentalStdlibApi
    override fun visitModule(element: IRModule, data: Store): Either<List<Byte>, SourceAnnotation> {
        val bc = buildChunk(element.startPos, element.endPos) {
            element.statements.forEach {
                when(val bc = visitStatement(it, data)) {
                    is Either.Left -> appendChild(bc.a)
                    is Either.Right -> return bc
                }
            }
        }
        return bc.left()
    }

    @ExperimentalStdlibApi
    override fun visitStatement(element: IRStatement, data: Store): Either<List<Byte>, SourceAnnotation> =
        when(element){
            is IRLegacyVar -> visitLegacyVar(element, data)
            is IRVar -> visitVar(element, data)
            is IRConst -> visitConst(element, data)
            else -> buildSourceAnnotation {
                message = "Failed to convert expression to bytecode"
                errorLine {
                    message = "$element is not yet implemented!"
                    start = element.startPos
                    end = element.endPos
                }
                sourceOrigin {
                    source = input
                    start = element.startPos
                    end = element.endPos
                }
            }.right()
        }

    @ExperimentalStdlibApi
    override fun visitLegacyVar(element: IRLegacyVar, data: Store): Either<List<Byte>, SourceAnnotation> {
        val bc = buildChunk(element.startPos, element.endPos) {
            when(val exprBc = visitExpression(element.expression, data)){
                is Either.Left -> appendChild(exprBc.a)
                is Either.Right -> return exprBc
            }
            data.names.add(element.name)
            append(Bytecode.PUSH_NAME){
                val idx = data.names.indexOfFirst { it == element.name }
                append(Operand.Integer(idx))
            }
        }
        return bc.left()
    }

    @ExperimentalStdlibApi
    override fun visitConst(element: IRConst, data: Store): Either<List<Byte>, SourceAnnotation> {
        val bc = buildChunk(element.startPos, element.endPos){
            append(Bytecode.HEAP)
            when(val exprBc = visitExpression(element.expression, data)){
                is Either.Left -> appendChild(exprBc.a)
                is Either.Right -> return exprBc
            }
            append(Bytecode.REF)
            data.names.add(element.name)
            append(Bytecode.PUSH_NAME){
                val idx = data.names.indexOfFirst { it == element.name }
                append(Operand.Integer(idx))
            }
        }
        return bc.left()
    }

    @ExperimentalStdlibApi
    override fun visitVar(element: IRVar, data: Store): Either<List<Byte>, SourceAnnotation> {
        val bc = buildChunk(element.startPos, element.endPos){
            append(Bytecode.HEAP)
            when(val exprBc = visitExpression(element.expression, data)){
                is Either.Left -> appendChild(exprBc.a)
                is Either.Right -> return exprBc
            }
            data.names.add(element.name)
            append(Bytecode.PUSH_NAME){
                val idx = data.names.indexOfFirst { it == element.name }
                append(Operand.Integer(idx))
            }
        }
        return bc.left()
    }

    @ExperimentalStdlibApi
    override fun visitExpression(element: IRExpression, data: Store): Either<List<Byte>, SourceAnnotation> =
        when(element){
            is IRBinary -> visitBinary(element, data)
            is IRConstant<*> -> visitConstant(element, data)
            is IRRef -> visitRef(element, data)
            else -> buildSourceAnnotation {
                message = "Failed to convert expression to bytecode"
                errorLine {
                    message = "$element is not yet implemented!"
                    start = element.startPos
                    end = element.endPos
                }
                sourceOrigin {
                    source = input
                    start = element.startPos
                    end = element.endPos
                }
            }.right()
        }

    @ExperimentalStdlibApi
    override fun visitRef(element: IRRef, data: Store): Either<List<Byte>, SourceAnnotation> {
        val bc = buildChunk(element.startPos, element.endPos){
            append(Bytecode.READ){
                val namei = if(element.refName in data.names){
                    data.names.indexOf(element.refName)
                }else{
                    -1
                }
                append(Operand.Integer(namei))
            }
        }
        return bc.left()
    }

    @ExperimentalStdlibApi
    override fun <T> visitConstant(element: IRConstant<T>, data: Store): Either<List<Byte>, SourceAnnotation> {
        val bc = buildChunk(element.startPos, element.endPos) {
            append(Bytecode.CONSTANT) {
                when (element.kind) {
                    is IRConstantKind.Int -> {
                        append(Operand.Integer(
                            if (data.constants.any { it.value == element.value }) {
                                data.constants.indexOfFirst {
                                    it.value == element.value
                                }
                            } else {
                                data.constants += Constant.ConstInt(element.value as Int)
                                data.constants.lastIndex
                            }
                        ))
                    }
                    else -> append(Operand.None)
                }
            }
        }
        return bc.left()
    }

    @ExperimentalStdlibApi
    override fun visitBinary(element: IRBinary, data: Store): Either<List<Byte>, SourceAnnotation> {
        val bc = buildChunk(element.startPos, element.endPos) {
            val left = when(val left = visitExpression(element.left, data)){
                is Either.Left -> left.a
                is Either.Right -> return left
            }
            appendChild(left)
            val right = when(val right = visitExpression(element.left, data)){
                is Either.Left -> right.a
                is Either.Right -> return right
            }
            appendChild(right)
            when (element) {
                is IRBinaryPlus -> append(Bytecode.ADD)
                is IRBinaryMinus -> append(Bytecode.SUB)
                is IRBinaryMult -> append(Bytecode.MUL)
                is IRBinaryDiv -> append(Bytecode.DIV)
            }
        }
        return bc.left()
    }
}