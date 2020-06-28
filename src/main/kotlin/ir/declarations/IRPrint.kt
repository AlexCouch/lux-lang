package ir.declarations

import buildPrettyString
import ir.visitors.IRElementVisitor
import ir.visitors.IRElementTransformer

class IRPrint(val expression: IRExpression, override var parent: IRStatementContainer?) : IRDeclaration{
    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitPrint(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        expression.transform(transformer, data)
    }

    override fun toString(): String =
        buildPrettyString{
            append("print $expression")
        }

}