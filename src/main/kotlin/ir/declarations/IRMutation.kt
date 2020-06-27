package ir.declarations

import buildPrettyString
import ir.visitors.IRElementVisitor
import ir.visitors.IRElementTransformer

class IRMutation(override val name: String, override var parent: IRStatementContainer?, val expression: IRExpression): IRDeclarationWithName{
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