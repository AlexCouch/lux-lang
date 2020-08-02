package ir.declarations.ssa

import ir.IRElement
import ir.declarations.IRExpression
import ir.declarations.IRStatementContainer
import ir.types.IRType
import ir.visitors.IRElementTransformer
import ir.visitors.IRElementVisitor

class PhiFunction(
    override val variables: List<SSAVar>,
    override val type: IRType,
    override var parent: IRStatementContainer?
): SSAElement, IRExpression{
    override val expressions: List<IRExpression>
        get() = variables.map { it.assignment }
    override val children: List<IRElement>
        get() = variables

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitPhi(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        children.forEach { it.transform(transformer, data) }
    }

}