package ir.declarations

import TokenPos
import buildPrettyString
import ir.symbol.IRMutationSymbol
import ir.symbol.IRSymbolOwner
import ir.types.IRType
import ir.visitors.IRElementVisitor
import ir.visitors.IRElementTransformer

class IRMutation(
    override val name: String,
    override var parent: IRStatementContainer?,
    val type: IRType = IRType.default,
    val expression: IRExpression,
    override val symbol: IRMutationSymbol,
    override val startPos: TokenPos,
    override val endPos: TokenPos
): IRDeclarationWithName, IRSymbolOwner{
    init{
        symbol.bind(this)
    }

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
        visitor.visitMutation(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        expression.transform(transformer, data)
    }

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            append("mut")
            append(" ")
            append("%$name")
            append(" = $expression")
        }

    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString {
            blue{
                append("mut")
            }
            append(" ")
            red{
                append("%$name")
            }
            append(" = ${expression.toPrettyString()}")
        }

}