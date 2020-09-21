package passes

import arrow.core.*
import errors.SourceAnnotation
import ir.IRElement
import ir.IRStatement
import ir.declarations.*
import ir.declarations.expressions.*
import ir.visitors.IRElementTransformer
import passes.symbolResolution.SymbolTable



/**
 * This prepares the IR for SSA/CFG analysis. This will separate compound expressions into unique variables.
 * This will aid in the SSA lowering and the construction of the CFG.
 *
 * Example:
 * ```
 * int const x = a^2 + b^2 + c^2
 * ```
 * Turns into
 * ```
 * %0 = a^2
 * %1 = b^2
 * %2 = %1 + %2
 * %3 = c^2
 * c = %2 + %3
 * ```
 * Binaries are considered axioms in the IR, along with variables, basic statements, jumps, comparisons, proc calls, etc.
 */
class PreSSATransformation : IRElementTransformer<SymbolTable>{
    val tempNameCounter = TemporaryNameCreator()

    override fun visitModule(element: IRModule, data: SymbolTable): Either<IRModule, SourceAnnotation> {
        val newModule = IRModule(element.name, null, element.symbol, element.startPos, element.endPos)
        element.convertChildrenToSSAForm(newModule, data)
        return newModule.left()
    }

    override fun visitProc(element: IRProc, data: SymbolTable): Either<IRProc, SourceAnnotation> {
        val irProc = data.declareProc(element.name, element.returnType, element.parent!!, element.startPos, element.endPos)
        irProc.params.addAll(element.params)
        element.convertChildrenToSSAForm(irProc, data)
        tempNameCounter.reset()
        return irProc.left()
    }

    private fun IRStatementContainer.convertChildrenToSSAForm(newContainer: IRStatementContainer, data: SymbolTable): Option<SourceAnnotation> {
        statements.forEach {
            when(it){
                is IRExpression -> {
                    val ssaForm = when(val result = it.convertToSSAForm(newContainer, data)){
                        is Either.Left -> result.a
                        is Either.Right -> return result.b.some()
                    }
                    newContainer.statements.add(ssaForm)
                }
                is IRVar -> {
                    val ssa = when(val result = it.expression.convertToSSAForm(newContainer, data)){
                        is Either.Left -> result.a
                        is Either.Right -> return result.b.some()
                    }
                    newContainer.statements.add(data.declareVariable(it.name, it.type, ssa, newContainer, it.startPos, it.endPos))
                }
                is IRConst -> {
                    val ssa = when(val result = it.expression.convertToSSAForm(newContainer, data)){
                        is Either.Left -> result.a
                        is Either.Right -> return result.b.some()
                    }
                    newContainer.statements.add(data.declareConst(it.name, it.type, ssa, newContainer, it.startPos, it.endPos))
                }
                is IRLegacyVar -> {
                    val ssa = when(val result = it.expression.convertToSSAForm(newContainer, data)){
                        is Either.Left -> result.a
                        is Either.Right -> return result.b.some()
                    }
                    newContainer.statements.add(data.declareLet(it.name, it.type, ssa, newContainer, it.startPos, it.endPos))
                }
                is IRProc -> {
                    val newProc = when(val result = visitProc(it, data)){
                        is Either.Left -> result.a
                        is Either.Right -> return result.b.some()
                    }
                    newContainer.statements.add(newProc)
                }
                else -> newContainer.statements.add(it)
            }
        }
        return none()
    }

    private fun IRExpression.convertToSSAForm(newContainer: IRStatementContainer, data: SymbolTable): Either<IRExpression, SourceAnnotation> {
        return when (this) {
            is IRBinary -> {
                val newLeft = when (left) {
                    is IRBinary -> {
                        val new = when (val result = left.convertToSSAForm(newContainer, data)) {
                            is Either.Left -> result.a
                            is Either.Right -> return result
                        }
                        if (!new.isPrimitive()) {
                            val const = data.declareConst(tempNameCounter.name, type, new, newContainer, startPos, endPos)
                            newContainer.statements.add(const)
                            data.declareReference(const.name, newContainer, startPos, endPos)
                        } else {
                            new
                        }
                    }
                    else -> left
                }
                val newRight = when (right) {
                    is IRBinary -> {
                        val new = when(val result = right.convertToSSAForm(newContainer, data)){
                            is Either.Left -> result.a
                            is Either.Right -> return result
                        }
                        if (!new.isPrimitive()) {
                            val const = data.declareConst(tempNameCounter.name, type, new, newContainer, startPos, endPos)
                            newContainer.statements.add(const)
                            data.declareReference(const.name, newContainer, startPos, endPos)
                        } else {
                            new
                        }
                    }
                    else -> right
                }
                if (!newLeft.isPrimitive()) {
                    val const = data.declareConst(tempNameCounter.name, type, newLeft, newContainer, startPos, endPos)
                    newContainer.statements.add(const)
                    data.declareReference(const.name, newContainer, startPos, endPos)
                }
                if (!newRight.isPrimitive()) {
                    val const = data.declareConst(tempNameCounter.name, type, newRight, newContainer, startPos, endPos)
                    newContainer.statements.add(const)
                    data.declareReference(const.name, newContainer, startPos, endPos)
                }
                val expr = when (kind) {
                    IRBinaryKind.PLUS -> IRBinaryPlus(newLeft, newRight, type, newContainer, startPos, endPos)
                    IRBinaryKind.MINUS -> IRBinaryMinus(newLeft, newRight, type, newContainer, startPos, endPos)
                    IRBinaryKind.MULT -> IRBinaryMult(newLeft, newRight, type, newContainer, startPos, endPos)
                    IRBinaryKind.DIV -> IRBinaryDiv(newLeft, newRight, type, newContainer, startPos, endPos)
                }
                if (
                    (newLeft.isPrimitive() || newLeft is IRRef) &&
                    (newRight.isPrimitive() || newRight is IRRef)
                ) {
                    expr.left()
                } else {
                    val const = data.declareConst(tempNameCounter.name, type, expr, newContainer, startPos, endPos)
                    newContainer.statements.add(const)
                    data.declareReference(const.name, newContainer, startPos, endPos).left()
                }
            }
            else -> this.left()
        }
    }

    override fun visitStatement(element: IRStatement, data: SymbolTable): Either<IRElement, SourceAnnotation> =
        when(element){
            is IRExpression -> visitExpression(element, data)
            is IRConst -> visitConst(element, data)
            is IRConstant<*> -> visitConstant(element, data)
            is IRProc -> visitProc(element, data)
            is IRPrint -> visitPrint(element, data)
            is IRMutation -> visitMutation(element, data)
            is IRLegacyVar -> visitLegacyVar(element, data)
            is IRVar -> visitVar(element, data)
            else -> TODO()
        }
}