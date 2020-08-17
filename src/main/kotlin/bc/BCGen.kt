package bc

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ir.IRElement
import ir.IRStatement
import ir.declarations.*
import ir.declarations.expressions.*
import ir.visitors.IRElementVisitor

inline class Instructions(val bytecode: ArrayList<Bytecode>)

class BytecodeGenerator: IRElementVisitor<Either<Instructions, String>, Store>{
    override fun visitElement(element: IRElement, data: Store): Either<Instructions, String> =
        when(element){
            is IRStatement -> visitStatement(element, data)
            else -> "Expected IRStatement but instead found $element".right()
        }

    override fun visitModule(element: IRModule, data: Store): Either<Instructions, String> {
        val moduleInstructions = Instructions(arrayListOf())
        element.statements.forEach {
            when(val bc = visitStatement(it, data)) {
                is Either.Left -> moduleInstructions.bytecode.addAll(bc.a.bytecode)
                is Either.Right -> return bc
            }
        }
        return moduleInstructions.left()
    }

    override fun visitStatement(element: IRStatement, data: Store): Either<Instructions, String> =
        when(element){
            is IRLet -> visitLet(element, data)
            is IRVar -> visitVar(element, data)
            is IRConst -> visitConst(element, data)
            else -> "$element is not implemented yet".right()
        }

    override fun visitLet(element: IRLet, data: Store): Either<Instructions, String> {
        val instructions = Instructions(arrayListOf())
        when(val exprBc = visitExpression(element.expression, data)){
            is Either.Left -> instructions.bytecode.addAll(exprBc.a.bytecode)
            is Either.Right -> return exprBc
        }
        data.names.add(element.name)
        return instructions.left()
    }

    override fun visitConst(element: IRConst, data: Store): Either<Instructions, String> {
        val instructions = Instructions(arrayListOf())
        when(val exprBc = visitExpression(element.expression, data)){
            is Either.Left -> instructions.bytecode.addAll(exprBc.a.bytecode)
            is Either.Right -> return exprBc
        }
        data.names.add(element.name)
        return instructions.left()
    }

    override fun visitVar(element: IRVar, data: Store): Either<Instructions, String> {
        val instructions = Instructions(arrayListOf())
        when(val exprBc = visitExpression(element.expression, data)){
            is Either.Left -> instructions.bytecode.addAll(exprBc.a.bytecode)
            is Either.Right -> return exprBc
        }
        data.names.add(element.name)
        return instructions.left()
    }

    override fun visitExpression(element: IRExpression, data: Store): Either<Instructions, String> =
        when(element){
            is IRBinary -> visitBinary(element, data)
            is IRConstant<*> -> visitConstant(element, data)
            is IRRef -> visitRef(element, data)
            else -> "$element is not yet implemented!".right()
        }

    override fun <T> visitConstant(element: IRConstant<T>, data: Store): Either<Instructions, String> {
        val instructions = Instructions(arrayListOf())
        instructions.bytecode += Bytecode.PUSH_NAME
        return super.visitConstant(element, data)
    }

    override fun visitBinary(element: IRBinary, data: Store): Either<Instructions, String> {
        val instructions = Instructions(arrayListOf())
        val left = when(val left = visitExpression(element.left, data)){
            is Either.Left -> left.a
            is Either.Right -> return left
        }
        instructions.bytecode += left.bytecode
        val right = when(val right = visitExpression(element.left, data)){
            is Either.Left -> right.a
            is Either.Right -> return right
        }
        instructions.bytecode += right.bytecode
        when (element) {
            is IRBinaryPlus -> instructions.bytecode += Bytecode.ADD
            is IRBinaryMinus -> instructions.bytecode += Bytecode.SUB
            is IRBinaryMult -> instructions.bytecode += Bytecode.MUL
            is IRBinaryDiv -> instructions.bytecode += Bytecode.DIV
        }
        return instructions.left()
    }
}