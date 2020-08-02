package ir.declarations.expressions.branching

import ir.declarations.IRExpression
import ir.declarations.IRStatementContainer
import ir.declarations.expressions.IRBlock
import ir.types.IRType
import ir.visitors.IRElementTransformer
import ir.visitors.IRElementVisitor

class IRBinaryConditional(
    override val condition: IRExpression,
    override val then: IRBlock,
    override val otherwise: IRBlock,
    override val type: IRType,
    override var parent: IRStatementContainer?
) : IRBranching {
    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitBinaryConditional(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        condition.transform(transformer, data)
        then.transform(transformer, data)
        otherwise.transform(transformer, data)
    }
}