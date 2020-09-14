package passes.symbolResolution

import TokenPos
import buildPrettyString
import ir.declarations.*
import ir.declarations.expressions.IRProcCall
import ir.declarations.expressions.IRRef
import ir.symbol.*
import ir.types.IRType
import sun.awt.Symbol
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet

interface SymbolTableBase<B : IRSymbolOwner, S : IRBindableSymbol<B>>{
    val unboundSymbols: LinkedHashSet<S>

    fun getByName(name: String): S?
    fun setByName(name: String, s: S)

    fun declare(name: String, createSymbol: () -> S, createOwner: (S) -> B): B{
        val existing = getByName(name)
        val symbol = if(existing == null){
            val new = createSymbol()
            setByName(name, new)
            new
        }else{
            unboundSymbols.remove(existing)
            existing
        }
        return createOwner(symbol)
    }

    fun declareIfNotExists(name: String, createSymbol: () -> S, createOwner: (S) -> B): B{
        val existing = getByName(name)
        val symbol = if(existing == null){
            val new = createSymbol()
            setByName(name, new)
            new
        }else{
            if(!existing.isBound) unboundSymbols.remove(existing)
            existing
        }
        return createOwner(symbol)
    }

    fun referenced(name: String, orElse: () -> S): S{
        return getByName(name) ?: run{
            val new = orElse()
            assert(unboundSymbols.add(new)){
                "Symbol for $new was already referenced"
            }
            setByName(name, new)
            new
        }
    }

}

class SpecializedSymbolTable<B: IRSymbolOwner, S: IRBindableSymbol<B>>: SymbolTableBase<B, S>{
    val nameToSymbol = linkedMapOf<String, S>()

    override fun getByName(name: String): S? = nameToSymbol[name]

    override fun setByName(name: String, s: S) {
        nameToSymbol[name] = s
    }

    operator fun get(name: String): S? = getByName(name)

    fun forEach(block: (S) -> Unit) = nameToSymbol.values.forEach(block)
    override val unboundSymbols: LinkedHashSet<S> = linkedSetOf()
}

abstract class OverridingSymbolTable<B, S>: SymbolTableBase<B, S>
        where B: IRSymbolOwner, S: IRBindableSymbol<B>{
    override val unboundSymbols: LinkedHashSet<S> = linkedSetOf()
    val nameToSymbol = LinkedList<Pair<String, S>>()

    override fun getByName(name: String): S? = nameToSymbol.find { (k, _) -> k == name }?.second

    override fun setByName(name: String, s: S) {
        nameToSymbol.add(name to s)
    }

    override fun declare(name: String, createSymbol: () -> S, createOwner: (S) -> B): B {
        val new = createSymbol()
        setByName(name, new)
        return createOwner(new)
    }

    open operator fun get(name: String): S? = getByName(name)
}

class ScopedOverridingSymbolTable<B, S>: ScopedSymbolTable<B, S>, OverridingSymbolTable<B, S>()
    where B: IRSymbolOwner, S: IRBindableSymbol<B> {
    override var currentScope: Scope<B, S>? = null

    override fun getByName(name: String): S? = currentScope?.get(name)
    override fun setByName(name: String, s: S) {
        currentScope?.set(name, s)
    }
    override fun declare(name: String, createSymbol: () -> S, createOwner: (S) -> B): B {
        val new = createSymbol()
        currentScope?.set(name, new)
        return createOwner(new)
    }

    override operator fun get(name: String): S? = getByName(name)
}

class Scope<B, S>(val owner: IRSymbolOwner, val parent: Scope<B, S>?)
    where B: IRSymbolOwner, S: IRBindableSymbol<B>{
    private val nameToSymbol = linkedMapOf<String, S>()

    private fun getByName(name: String): S? = nameToSymbol[name] ?: parent?.getByName(name)

    operator fun get(name: String): S? = getByName(name)

    fun getLocal(name: String) = getByName(name)

    operator fun set(name: String, s: S){
        nameToSymbol[name] = s
    }

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString{
            appendWithNewLine("scope{")
            indent {
                appendWithNewLine("%owner = $owner")
                appendWithNewLine("%parent: $parent")
                appendWithNewLine("%symbols = [")
                indent {
                    nameToSymbol.forEach { (name, s) ->
                        appendWithNewLine("symbol %$name = $s")
                    }
                }
                appendWithNewLine("]")
            }
            append("}")
        }

    fun forEach(block: (S) -> Unit) = nameToSymbol.values.forEach(block)

}

class SimpleOverridingSymbolTable<B: IRSymbolOwner, S: IRBindableSymbol<B>>: OverridingSymbolTable<B, S>()

interface ScopedSymbolTable<B: IRSymbolOwner, S: IRBindableSymbol<B>>: SymbolTableBase<B, S>{
    var currentScope: Scope<B, S>?

    override fun getByName(name: String): S?{
        val scope = currentScope ?: return null
        return scope[name]
    }

    override fun setByName(name: String, s: S) {
        val scope = currentScope ?: throw AssertionError("No active scope!")
        scope[name] = s
    }

    operator fun get(name: String) = getByName(name)

    fun declareLocal(name: String, createSymbol: () -> S, createOwner: (S) -> B): B{
        val symbol = getByName(name) ?: createSymbol().also { setByName(name, it) }
        return createOwner(symbol)
    }

    fun introduceLocal(name: String, symbol: S){
        val scope = currentScope ?: throw AssertionError("No active scope!")
        scope[name]?.let {
            throw AssertionError("$name is already bound to $it")
        }
        scope[name] = symbol
    }

    fun enterScope(owner: IRSymbolOwner){
        currentScope = Scope(owner, currentScope)
    }

    fun exitScope(owner: IRSymbolOwner){
        currentScope?.owner.let {
            assert(it == owner){
                "Unexpected leaveScope: owner=$owner, currentScope.owner=$it"
            }
        }

        currentScope = currentScope?.parent

        if(currentScope != null && unboundSymbols.isNotEmpty()){
            throw AssertionError("Local scope contains unbound symbols: ${unboundSymbols.joinToString { it.toString() }}")
        }
    }

    fun forEach(block: (S) -> Unit) = currentScope?.forEach(block)
}

class SymbolTable{
    private val moduleSymbolTable = SpecializedSymbolTable<IRModule, IRModuleSymbol>()
    private val varSymbolTable = ScopedOverridingSymbolTable<IRVar, IRVarSymbol>()
    private val letSymbolTable = ScopedOverridingSymbolTable<IRLet, IRLetSymbol>()
    private val constSymbolTable = ScopedOverridingSymbolTable<IRConst, IRConstSymbol>()
    private val procSymbolTable = SpecializedSymbolTable<IRProc, IRProcSymbol>()
    private val procParamSymbolTable = SpecializedSymbolTable<IRProcParam, IRProcParamSymbol>()
    private val refSymbolTable = ScopedOverridingSymbolTable<IRRef, IRRefSymbol>()
    private val procCallSymbolTable = ScopedOverridingSymbolTable<IRProcCall, IRProcCallSymbol>()
    private val mutationSymbolTable = ScopedOverridingSymbolTable<IRMutation, IRMutationSymbol>()

    private val scopedSymbolTables = listOf(
        varSymbolTable,
        letSymbolTable,
        constSymbolTable,
        refSymbolTable,
        procCallSymbolTable,
        mutationSymbolTable
    )

    fun declareModule(name: String) = moduleSymbolTable.declare(name, { IRModuleSymbol() }, { IRModule(name, null, it, TokenPos.default) })

    fun hasVariable(name: String) = findVariable(name) != null

    fun declareVariable(
        name: String,
        type: IRType,
        expression: IRExpression,
        parent: IRStatementContainer,
        position: TokenPos,
        variableFactory: (IRVarSymbol) -> IRVar = {
            IRVar(name, type, expression, parent, it, position)
        }
    ) = varSymbolTable.declareLocal(name, { IRVarSymbol() }, variableFactory)

    fun referenceVariable(name: String) = varSymbolTable.getByName(name)

    fun declareLet(
        name: String,
        type: IRType,
        expression: IRExpression,
        parent: IRStatementContainer,
        position: TokenPos,
        variableFactory: (IRLetSymbol) -> IRLet = {
            IRLet(name, type, expression, parent, it, position)
        }
    ) = letSymbolTable.declareLocal(name, { IRLetSymbol() }, variableFactory)

    fun referenceLet(name: String) = letSymbolTable.getByName(name)

    fun declareConst(
        name: String,
        type: IRType,
        expression: IRExpression,
        parent: IRStatementContainer?,
        position: TokenPos,
        variableFactory: (IRConstSymbol) -> IRConst = {
            IRConst(name, type, expression, parent, it, position)
        }
    ) = constSymbolTable.declareLocal(name, { IRConstSymbol() }, variableFactory)

    fun referenceConst(name: String) = constSymbolTable.getByName(name)

    fun findVariable(name: String) =
        varSymbolTable[name] ?:
        letSymbolTable[name] ?:
        constSymbolTable[name] ?:
        procParamSymbolTable[name]

    fun declareProc(
        name: String,
        returnType: IRType,
        parent: IRStatementContainer,
        position: TokenPos,
        procFactory: (IRProcSymbol) -> IRProc = {
            IRProc(name, returnType, parent, it, position)
        }
    ) = procSymbolTable.declare(name, { IRProcSymbol() },  procFactory)

    fun findProc(name: String) = procSymbolTable[name]

    fun declareProcParam(
        procName: String,
        paramName: String,
        type: IRType = IRType.default,
        position: TokenPos
    ): IRProcParam {
        val proc = findProc(procName) ?: throw IllegalArgumentException("Could not find procedure with symbol $procName")
        val param = procParamSymbolTable.declare(paramName, { IRProcParamSymbol() }, {IRProcParam(paramName, proc.owner, type, it, position)})
        proc.owner?.params?.add(param)
        return param
    }

    fun declareReference(name: String, parent: IRStatementContainer?, position: TokenPos) = refSymbolTable.declare(name, { IRRefSymbol() }, { IRRef(name, IRType.default, it, parent, position) })

    fun findReference(name: String) = refSymbolTable[name]

    fun declareProcCall(name: String, arguments: ArrayList<IRExpression>, parent: IRStatementContainer?, position: TokenPos) =
        procCallSymbolTable.declare(name, {
            IRProcCallSymbol()
        }, {
            IRProcCall(
                name,
                arguments,
                IRType.default,
                it,
                parent,
                position
            )
        })

    fun findProcCall(name: String) = procCallSymbolTable[name]

    fun declareMutation(
        name: String,
        parent: IRStatementContainer,
        expression: IRExpression,
        position: TokenPos
    ) =
        mutationSymbolTable.declare(
            name, {
                IRMutationSymbol()
            }, {
                IRMutation(name, parent, expression.type, expression, it, position)
            }
        )

    fun findMutation(name: String) = mutationSymbolTable[name]

    fun forEachReference(block: (IRRefSymbol) -> Unit) = refSymbolTable.forEach(block)
    fun forEachMutation(block: (IRMutationSymbol) -> Unit) = mutationSymbolTable.forEach(block)
    fun forEachProcCall(block: (IRProcCallSymbol) -> Unit) = procCallSymbolTable.forEach(block)

    fun forEachProc(block: (IRProcSymbol) -> Unit) = procSymbolTable.forEach(block)
    fun forEachVar(block: (IRVarSymbol) -> Unit) = varSymbolTable.forEach(block)
    fun forEachLet(block: (IRLetSymbol) -> Unit) = letSymbolTable.forEach(block)
    fun forEachConst(block: (IRConstSymbol) -> Unit) = constSymbolTable.forEach(block)

    fun enterScope(owner: IRSymbolOwner){
        scopedSymbolTables.forEach {
            it.enterScope(owner)
        }
    }

    fun leaveScope(owner: IRSymbolOwner){
        scopedSymbolTables.forEach {
            it.exitScope(owner)
        }
    }
}