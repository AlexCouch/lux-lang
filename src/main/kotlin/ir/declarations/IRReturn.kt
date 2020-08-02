package ir.declarations

import buildPrettyString
import ir.IRStatement
import ir.visitors.IRElementTransformer
import ir.visitors.IRElementVisitor

class IRReturn(val expr: IRExpression, override var parent: IRStatementContainer?) : IRStatement{
    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
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
    fun toPrettyString(): String =
        buildPrettyString {
            blue{
                append("ret")
            }
            append(" $expr")
        }
}