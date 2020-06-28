package ir.declarations

import ir.symbol.IRSymbol
import ir.symbol.IRSymbolOwner
import ir.symbol.IRVarSymbol
import ir.types.IRType

interface IRVarDeclaration<S : IRSymbol>: IRDeclarationWithName, IRSymbolOwner{
    val type: IRType
    val expression: IRExpression
    override val symbol: S
}

enum class IRVarKind{
    /**
     * Shared Immutable binding to current frame
     *
     * References = Pointers
     */
    CONST,

    /**
     * Shared Mutable binding to current frame
     *
     * References = Pointers
     */
    VAR,

    /**
     * Unshared Mutable binding to current frame
     *
     * References = Values
     */
    LET
}