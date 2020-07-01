package ir.builtin

import ir.symbol.IRSymbol
import ir.types.IRSimpleType
import ir.types.IRType

enum class BuiltinTypes(val symbol: String){
    INT("int"),
    DYNAMIC("dyn")
    ;

    fun makeSimpleType() = IRSimpleType(this.symbol)

    companion object{
        fun fromSymbol(symbol: String): BuiltinTypes? = BuiltinTypes.values().find{
            it.symbol == symbol
        }
    }
}