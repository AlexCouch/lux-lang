package ir.declarations

import buildPrettyString
import ir.symbol.IRMutationSymbol
import ir.symbol.IRSymbol
import ir.symbol.IRSymbolOwner
import ir.types.IRType
import ir.visitors.IRElementVisitor
import ir.visitors.IRElementTransformer

class IRMutation(
    override val name: String,
    override var parent: IRStatementContainer?,
    val type: IRType = IRType.default,
    val expression: IRExpression,
    override val symbol: IRMutationSymbol
): IRDeclarationWithName, IRSymbolOwner{
    init{
        symbol.bind(this)
    }

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitMutation(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        expression.transform(transformer, data)
    }

    override fun toString(): String =
        buildPrettyString {
            append("mut %$name = $expression")
        }

}