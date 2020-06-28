package ir.symbol

import ir.declarations.*
import ir.declarations.expressions.IRProcCall
import ir.declarations.expressions.IRRef

interface IRSymbol{
    val owner: IRSymbolOwner
    val isBound: Boolean
}

interface IRSymbolOwner{
    val symbol: IRSymbol
}

interface IRBindableSymbol<S: IRSymbolOwner>: IRSymbol{
    override val owner: S

    fun bind(owner: S)
}

abstract class IRBindableSymbolBase<S: IRSymbolOwner>: IRBindableSymbol<S>{
    private var _owner: S? = null
    override val owner: S get() = _owner ?: throw IllegalStateException("Symbol with ${javaClass.simpleName} is unbound")

    override val isBound: Boolean get() = _owner != null

    override fun bind(owner: S) {
        if(_owner == null){
            _owner = owner
        }else{
            throw IllegalStateException("${javaClass.simpleName} is already bound: $owner")
        }
    }
}

class IRProcSymbol: IRBindableSymbolBase<IRProc>()

abstract class IRVarSymbolBase<S: IRVarDeclaration<*>> : IRBindableSymbolBase<S>()

class IRModuleSymbol : IRBindableSymbolBase<IRModule>()

class IRVarSymbol : IRVarSymbolBase<IRVar>()
class IRLetSymbol : IRVarSymbolBase<IRLet>()
class IRConstSymbol : IRVarSymbolBase<IRConst>()

class IRRefSymbol : IRBindableSymbolBase<IRRef>()
class IRProcCallSymbol : IRBindableSymbolBase<IRProcCall>()
class IRMutationSymbol : IRBindableSymbolBase<IRMutation>()