package passes.typecheck

import ir.IRElement
import ir.IRStatement
import ir.declarations.*
import ir.declarations.expressions.*
import ir.symbol.IRProcParamSymbol
import ir.symbol.IRVarSymbolBase
import ir.types.IRType
import ir.visitors.IRElementTransformer
import passes.symbolResolution.SymbolTable

class TypeCheckingPass : IRElementTransformer<SymbolTable>{
    override fun visitModule(element: IRModule, data: SymbolTable): IRModule {
        val transformedModule = data.declareModule(element.name)
        data.enterScope(transformedModule)
        val transformedStats = element.statements.map {
            visitStatement(it, data)
        }
        transformedModule.statements.addAll(transformedStats)
        data.leaveScope(transformedModule)
        return transformedModule
    }

    override fun visitStatement(element: IRStatement, data: SymbolTable): IRStatement {
        return when (element) {
            is IRConst -> element.checkAndInferType(data)
            is IRVar -> element.checkAndInferType(data)
            is IRLet -> element.checkAndInferType(data)
            is IRProc -> visitProc(element, data)
            else -> element
        }
    }

    override fun visitProc(element: IRProc, data: SymbolTable): IRProc {
        val proc = data.declareProc(element.name, element.returnType, element.parent!!)
        data.enterScope(proc)
        val statements = element.statements.map {
            when(it){
                is IRReturn -> {
                    val expr = visitExpression(it.expr, data)
                    if(expr.type != element.returnType){
                        if(element.returnType != IRType.default){
                            throw IllegalArgumentException("Return expression does not match procedure return type, expected type ${element.returnType} but instead found ${it.expr.type}")
                        }
                    }
                    it
                }
                is IRVarDeclaration<*> -> it.checkAndInferType(data)
                else -> it
            }
        }
        proc.params.addAll(element.params)
        proc.statements.addAll(statements)
        data.leaveScope(proc)
        return proc
    }

    private fun IRVarDeclaration<*>.checkAndInferType(data: SymbolTable): IRVarDeclaration<*>{
        val expr = visitExpression(expression, data)
        if(type != expr.type){
            return when (this) {
                is IRConst -> data.declareConst(name, expr.type, expr, parent!!)
                is IRLet -> data.declareLet(name, expr.type, expr, parent!!)
                is IRVar -> data.declareVariable(name, expr.type, expr, parent!!)
                else -> throw IllegalArgumentException("This should never happen, unless in development mode. Expected an IRVarDeclaration while checking and inferring type but instead got $this")
            }
        }
        return this
    }

    override fun visitExpression(element: IRExpression, data: SymbolTable): IRExpression {
        element.apply {
            when(this){
                is IRBinary -> {
                    val lefty = visitExpression(left, data)
                    val righty = visitExpression(right, data)
                    if(lefty.type == righty.type){
                        return when(this){
                            is IRBinaryPlus -> IRBinaryPlus(lefty, righty, lefty.type, parent)
                            is IRBinaryMinus -> IRBinaryMinus(lefty, righty, lefty.type, parent)
                            is IRBinaryMult -> IRBinaryMult(lefty, righty, lefty.type, parent)
                            is IRBinaryDiv -> IRBinaryDiv(lefty, righty, lefty.type, parent)
                            else -> this
                        }
                    }
                }
                is IRProcCall -> {
                    val target = data.findProc(name)?.owner ?: throw IllegalArgumentException("Could not find procedure $name")
                    arguments.zip(target.params).forEach { (arg, param) ->
                        require(param.type == IRType.default || (arg.type == param.type)){
                            "Expected an argument of type ${param.type} but instead got ${arg.type}"
                        }
                    }
                }
                is IRRef -> {
                    val variable = data.findVariable(refName) ?: throw IllegalArgumentException("No variable with symbol $refName is declared")
                    val type = if(variable is IRVarSymbolBase<*>) variable.owner!!.type else if(variable is IRProcParamSymbol) variable.owner!!.type else throw IllegalArgumentException("Could not get type from variable with symbol $refName")
                    return if(type != IRType.default){
                        IRRef(refName, type, symbol, parent)
                    }else{
                        element
                    }
                }
                else -> return this
            }
        }
        return element
    }
}