package ir.declarations

import ir.IRElement
import ir.IRStatement
import ir.declarations.expressions.IRConstant
import ir.declarations.expressions.IRRef
import ir.types.IRType
import ir.visitors.IRElementTransformer

interface IRExpression : IRStatement{
    val type: IRType

    override fun <D> transform(transformer: IRElementTransformer<D>, data: D) =
        accept(transformer, data)

    fun isPrimitive(): Boolean =
        this is IRConstant<*> || this is IRRef
}