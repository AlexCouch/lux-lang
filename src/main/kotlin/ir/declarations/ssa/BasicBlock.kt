package ir.declarations.ssa

import ir.IRElement
import ir.IRStatement
import ir.declarations.IRExpression
import ir.declarations.IRStatementContainer
import ir.declarations.IRVarDeclaration
import ir.visitors.IRElementVisitor

class BasicBlock(
    override val statements: ArrayList<IRStatement>,
    override val name: String,
    override var parent: IRStatementContainer?
) : IRStatementContainer, SSAElement{
    override val variables: List<SSAVar>
        get() =
            statements.filter{
                it is IRVarDeclaration<*>
            }.map {
                (it as IRVarDeclaration<*>).toSSAVar()
            }

    override val children: List<IRElement>
        get() = statements

    override val expressions: List<IRExpression>
        get() = statements.filterIsInstance<IRExpression>()

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitBasicBlock(this, data)

}