package ir.symbol

import ir.declarations.*
import ir.declarations.expressions.IRProcCall
import ir.declarations.expressions.IRRef

interface IRSymbol{
    val owner: IRSymbolOwner?
    val isBound: Boolean
}

interface IRSymbolOwner{
    val symbol: IRSymbol
}

abstract class IRBindableSymbol<S: IRSymbolOwner>: IRSymbol{
    abstract override var owner: S?
        protected set

    abstract fun bind(owner: S)
}

abstract class IRBindableSymbolBase<S: IRSymbolOwner>: IRBindableSymbol<S>(){
    override var owner: S? = null
        get() = field ?: throw IllegalStateException("Symbol with ${javaClass.simpleName} is unbound")

    override val isBound: Boolean get() = owner != null

    override fun bind(owner: S) {
        this.owner = owner
    }
}

class IRProcSymbol: IRBindableSymbolBase<IRProc>()
class IRProcParamSymbol: IRBindableSymbolBase<IRProcParam>()

abstract class IRVarSymbolBase<S: IRVarDeclaration<*>> : IRBindableSymbolBase<S>()

class IRModuleSymbol : IRBindableSymbolBase<IRModule>()

class IRVarSymbol : IRVarSymbolBase<IRVar>()
class IRLetSymbol : IRVarSymbolBase<IRLegacyVar>()
class IRConstSymbol : IRVarSymbolBase<IRConst>()

class IRRefSymbol : IRBindableSymbolBase<IRRef>()
class IRProcCallSymbol : IRBindableSymbolBase<IRProcCall>()
class IRMutationSymbol : IRBindableSymbolBase<IRMutation>()