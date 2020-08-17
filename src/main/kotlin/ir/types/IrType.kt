package ir.types

import buildPrettyString
import ir.builtin.BuiltinTypes
import ir.symbol.IRSymbol

interface TypeMarker

interface IRType : TypeMarker{
    companion object{
        val default = BuiltinTypes.DYNAMIC.makeSimpleType()
        val string = BuiltinTypes.STR.makeSimpleType()
    }
    fun toPrettyString(): String
}
interface IRSimpleTypeMarker: TypeMarker
interface IRDynamicTypeMarker: TypeMarker
interface IRUnresolvedTypeMarker: TypeMarker

class IRSimpleType(val symbol: String) : IRType, IRSimpleTypeMarker{
    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString{
            append(symbol)
        }

    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString{
            green{
                append(symbol)
            }
        }

    override fun equals(other: Any?): Boolean =
        this.symbol == (other as? IRSimpleType)?.symbol

    override fun hashCode(): Int {
        return symbol.hashCode()
    }
}

interface IRDynamicType : IRType, IRDynamicTypeMarker