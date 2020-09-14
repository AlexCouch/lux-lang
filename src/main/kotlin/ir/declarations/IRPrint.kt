package ir.declarations

import TokenPos
import buildPrettyString
import ir.visitors.IRElementVisitor
import ir.visitors.IRElementTransformer

class IRPrint(val expression: IRExpression, override var parent: IRStatementContainer?,
              override val position: TokenPos) : IRDeclaration{
    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
        visitor.visitPrint(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        expression.transform(transformer, data)
    }

    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString{
            blue{
                append("print")
            }
            append(" ${expression.toPrettyString()}")
        }

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString{
            append("print")
            append(" $expression")
        }

}