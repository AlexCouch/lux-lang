package passes.typecheck

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import errors.SourceAnnotation
import errors.buildSourceAnnotation
import ir.IRStatement
import ir.declarations.*
import ir.declarations.expressions.*
import ir.symbol.IRProcParamSymbol
import ir.symbol.IRVarSymbolBase
import ir.types.IRType
import ir.visitors.IRElementTransformer
import passes.symbolResolution.SymbolTable

class TypeCheckingPass : IRElementTransformer<SymbolTable>{
    override fun visitModule(element: IRModule, data: SymbolTable): Either<IRModule, SourceAnnotation> {
        val transformedModule = data.declareModule(element.name)
        data.enterScope(transformedModule)
        val transformedStats = element.statements.map {
            when(val result = visitStatement(it, data)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
        }
        transformedModule.statements.addAll(transformedStats)
        data.leaveScope(transformedModule)
        return transformedModule.left()
    }

    override fun visitStatement(element: IRStatement, data: SymbolTable): Either<IRStatement, SourceAnnotation> {
        return when (element) {
            is IRConst -> element.checkAndInferType(data)
            is IRVar -> element.checkAndInferType(data)
            is IRLegacyVar -> element.checkAndInferType(data)
            is IRProc -> visitProc(element, data)
            else -> element.left()
        }
    }

    override fun visitProc(element: IRProc, data: SymbolTable): Either<IRProc, SourceAnnotation> {
        val proc = data.declareProc(element.name, element.returnType, element.parent!!, element.startPos, element.endPos)
        data.enterScope(proc)
        val statements = element.statements.map {
            when(it){
                is IRReturn -> {
                    val expr = when(val result = visitExpression(it.expr, data)){
                        is Either.Left -> result.a
                        is Either.Right -> return result
                    }
                    if(expr.type != element.returnType){
                        if(element.returnType != IRType.default){
                            throw IllegalArgumentException("Return expression does not match procedure return type, expected type ${element.returnType} but instead found ${it.expr.type}")
                        }
                    }
                    it
                }
                is IRVarDeclaration<*> -> when(val result = it.checkAndInferType(data)){
                    is Either.Left -> result.a
                    is Either.Right -> return result
                }
                else -> it
            }
        }
        proc.params.addAll(element.params)
        proc.statements.addAll(statements)
        data.leaveScope(proc)
        return proc.left()
    }

    private fun IRVarDeclaration<*>.checkAndInferType(data: SymbolTable): Either<IRVarDeclaration<*>, SourceAnnotation>{
        val expr = when(val result = visitExpression(expression, data)){
            is Either.Left -> result.a
            is Either.Right -> return result
        }
        if(type != expr.type){
            return when (this) {
                is IRConst -> data.declareConst(name, expr.type, expr, parent!!, startPos, endPos).left()
                is IRLegacyVar -> data.declareLet(name, expr.type, expr, parent!!, startPos, endPos).left()
                is IRVar -> data.declareVariable(name, expr.type, expr, parent!!, startPos, endPos).left()
                else -> buildSourceAnnotation {
                    message =
                        "This should never happen, unless in development mode. Expected an IRVarDeclaration while checking and inferring type but instead got $this"
                    errorLine {
                        start = this@checkAndInferType.startPos
                    }
                }.right()
            }
        }
        return this.left()
    }

    override fun visitExpression(element: IRExpression, data: SymbolTable): Either<IRExpression, SourceAnnotation> {
        element.apply {
            when(this){
                is IRBinary -> {
                    val lefty = when(val result = visitExpression(left, data)){
                        is Either.Left -> result.a
                        is Either.Right -> return result
                    }
                    val righty = when(val result = visitExpression(right, data)){
                        is Either.Left -> result.a
                        is Either.Right -> return result
                    }
                    if(lefty.type == righty.type){
                        return when(this){
                            is IRBinaryPlus -> IRBinaryPlus(lefty, righty, lefty.type, parent, startPos, endPos).left()
                            is IRBinaryMinus -> IRBinaryMinus(lefty, righty, lefty.type, parent, startPos, endPos).left()
                            is IRBinaryMult -> IRBinaryMult(lefty, righty, lefty.type, parent, startPos, endPos).left()
                            is IRBinaryDiv -> IRBinaryDiv(lefty, righty, lefty.type, parent, startPos, endPos).left()
                            else -> this.left()
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
                        IRRef(refName, type, symbol, parent, startPos, endPos).left()
                    }else{
                        element.left()
                    }
                }
                else -> return this.left()
            }
        }
        return element.left()
    }
}