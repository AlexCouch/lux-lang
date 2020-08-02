package ir.declarations.expressions

import buildPrettyString
import ir.declarations.IRExpression
import ir.declarations.IRStatementContainer
import ir.symbol.IRProcCallSymbol
import ir.symbol.IRSymbol
import ir.symbol.IRSymbolOwner
import ir.types.IRType
import ir.visitors.IRElementTransformer
import ir.visitors.IRElementVisitor

class IRProcCall(
    val name: String,
    val arguments: ArrayList<IRExpression> = arrayListOf(),
    override val type: IRType = IRType.default,
    override val symbol: IRProcCallSymbol,
    override var parent: IRStatementContainer?
) : IRExpression, IRSymbolOwner{

    init{
        symbol.bind(this)
    }

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitProcCall(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        //
    }

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString{
            append("call")
            append(" ")
            append("%$name")
            append("(")
            arguments.forEachIndexed { i, it ->
                append(it.toString())
                if(i < arguments.size - 1) append(", ")
            }
            append(")")
        }
    @ExperimentalStdlibApi
    fun toPrettyString(): String =
        buildPrettyString{
            blue {
                append("call")
            }
            append(" ")
            red{
                append("%$name")
            }
            append("(")
            arguments.forEachIndexed { i, it ->
                append(it.toString())
                if(i < arguments.size - 1) append(", ")
            }
            append(")")
        }
}