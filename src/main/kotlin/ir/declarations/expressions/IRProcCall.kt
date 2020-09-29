package ir.declarations.expressions

import TokenPos
import buildPrettyString
import ir.declarations.IRExpression
import ir.declarations.IRStatementContainer
import ir.symbol.IRProcCallSymbol
import ir.symbol.IRSymbolOwner
import ir.types.IRType
import ir.visitors.IRElementTransformer
import ir.visitors.IRElementVisitor

class IRProcCall(
    val name: String,
    val arguments: ArrayList<IRExpression> = arrayListOf(),
    override val type: IRType = IRType.default,
    override val symbol: IRProcCallSymbol,
    override var parent: IRStatementContainer?,
    override val startPos: TokenPos,
    override val endPos: TokenPos
) : IRExpression, IRSymbolOwner{

    init{
        symbol.bind(this)
    }

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
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
    override fun toPrettyString(): String =
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
                append(it.toPrettyString())
                if(i < arguments.size - 1) append(", ")
            }
            append(")")
        }
}