package ir.declarations.ssa

import ir.IRElement
import ir.IRStatement
import ir.declarations.IRDeclarationWithName
import ir.declarations.IRExpression
import ir.declarations.IRStatementContainer
import ir.declarations.IRVarDeclaration
import ir.visitors.IRElementTransformer
import ir.visitors.IRElementVisitor

class SSAVar(
    override val name: String,
    val assignment: IRExpression,
    override var parent: IRStatementContainer?
) : IRDeclarationWithName, SSAElement{
    override val variables: List<SSAVar>
        get() = emptyList()
    override val expressions: List<IRExpression>
        get() = listOf(assignment)
    override val children: List<IRElement>
        get() = expressions

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitSSAVar(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        assignment.transform(transformer, data)
    }

}

fun IRVarDeclaration<*>.toSSAVar() = SSAVar(this.name, this.expression, this.parent)