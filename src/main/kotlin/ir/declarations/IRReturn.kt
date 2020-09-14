package ir.declarations

import TokenPos
import buildPrettyString
import ir.IRStatement
import ir.visitors.IRElementTransformer
import ir.visitors.IRElementVisitor

class IRReturn(val expr: IRExpression, override var parent: IRStatementContainer?, override val position: TokenPos) : IRStatement{
    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
        visitor.visitReturn(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        expr.transform(transformer, data)
    }

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            append("ret")
            append(" $expr")
        }
    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString {
            blue{
                append("ret")
            }
            append(" ${expr.toPrettyString()}")
        }
}