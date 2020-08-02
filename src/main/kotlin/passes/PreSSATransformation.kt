package passes

import ir.IRElement
import ir.IRStatement
import ir.declarations.*
import ir.declarations.expressions.*
import ir.symbol.IRMutationSymbol
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

    override fun visitModule(element: IRModule, data: SymbolTable): IRModule {
        val newModule = IRModule(element.name, null, element.symbol)
        element.convertChildrenToSSAForm(newModule, data)
        return newModule
    }

    override fun visitProc(element: IRProc, data: SymbolTable): IRProc {
        val irProc = data.declareProc(element.name, element.returnType, element.parent!!)
        irProc.params.addAll(element.params)
        element.convertChildrenToSSAForm(irProc, data)
        return irProc
    }

    private fun IRStatementContainer.convertChildrenToSSAForm(newContainer: IRStatementContainer, data: SymbolTable){
        statements.forEach {
            when(it){
                is IRExpression -> {
                    val ssaForm = it.convertToSSAForm(newContainer, data)
                    newContainer.statements.add(ssaForm)
                }
                is IRVar -> {
                    val ssa = it.expression.convertToSSAForm(newContainer, data)
                    newContainer.statements.add(data.declareVariable(it.name, it.type, ssa, newContainer))
                }
                is IRConst -> {
                    val ssa = it.expression.convertToSSAForm(newContainer, data)
                    newContainer.statements.add(data.declareConst(it.name, it.type, ssa, newContainer))
                }
                is IRLet -> {
                    val ssa = it.expression.convertToSSAForm(newContainer, data)
                    newContainer.statements.add(data.declareLet(it.name, it.type, ssa, newContainer))
                }
                is IRProc -> {
                    val newProc = visitProc(it, data)
                    newContainer.statements.add(newProc)
                }
                else -> newContainer.statements.add(it)
            }
        }
    }

    private fun IRExpression.convertToSSAForm(newContainer: IRStatementContainer, data: SymbolTable): IRExpression =
        when(this){
            is IRBinary -> {
                val newLeft = when(left){
                    is IRBinary -> {
                        val new = left.convertToSSAForm(newContainer, data)
                        if(!new.isPrimitive()){
                            val const = data.declareConst(tempNameCounter.name, type, new, newContainer)
                            newContainer.statements.add(const)
                            data.declareReference(const.name, newContainer)
                        }else{
                            new
                        }
                    }
                    else -> left
                }
                val newRight = when(right){
                    is IRBinary -> {
                        val new = right.convertToSSAForm(newContainer, data)
                        if(!new.isPrimitive()){
                            val const = data.declareConst(tempNameCounter.name, type, new, newContainer)
                            newContainer.statements.add(const)
                            data.declareReference(const.name, newContainer)
                        }else{
                            new
                        }
                    }
                    else -> right
                }
                if(!newLeft.isPrimitive()){
                    val const = data.declareConst(tempNameCounter.name, type, newLeft, newContainer)
                    newContainer.statements.add(const)
                    data.declareReference(const.name, newContainer)
                }
                if(!newRight.isPrimitive()){
                    val const = data.declareConst(tempNameCounter.name, type, newRight, newContainer)
                    newContainer.statements.add(const)
                    data.declareReference(const.name, newContainer)
                }
                val expr = when(kind){
                    IRBinaryKind.PLUS -> IRBinaryPlus(newLeft, newRight, type, newContainer)
                    IRBinaryKind.MINUS -> IRBinaryMinus(newLeft, newRight, type, newContainer)
                    IRBinaryKind.MULT -> IRBinaryMult(newLeft, newRight, type, newContainer)
                    IRBinaryKind.DIV -> IRBinaryDiv(newLeft, newRight, type, newContainer)
                }
                if(
                    (newLeft.isPrimitive() || newLeft is IRRef) &&
                    (newRight.isPrimitive() || newRight is IRRef)
                ){
                    expr
                }else {
                    val const = data.declareConst(tempNameCounter.name, type, expr, newContainer)
                    newContainer.statements.add(const)
                    data.declareReference(const.name, newContainer)
                }
            }
            else -> this
        }

    override fun visitStatement(element: IRStatement, data: SymbolTable): IRElement =
        when(element){
            is IRExpression -> visitExpression(element, data)
            is IRConst -> visitConst(element, data)
            is IRConstant<*> -> visitConstant(element, data)
            is IRProc -> visitProc(element, data)
            is IRPrint -> visitPrint(element, data)
            is IRMutation -> visitMutation(element, data)
            is IRLet -> visitLet(element, data)
            is IRVar -> visitVar(element, data)
            else -> TODO()
        }
}