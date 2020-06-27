package ir.declarations

import ir.IRElement
import ir.IRStatement
import ir.types.IRType
import ir.visitors.IRElementTransformer

interface IRExpression : IRStatement{
    val type: IRType

    override fun <D> transform(transformer: IRElementTransformer<D>, data: D): IRElement =
        accept(transformer, data) as IRExpression
}