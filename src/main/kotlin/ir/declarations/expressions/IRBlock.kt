package ir.declarations.expressions

import buildPrettyString
import ir.IRStatement
import ir.declarations.IRExpression
import ir.declarations.IRStatementContainer
import ir.types.IRType
import ir.visitors.IRElementVisitor

class IRBlock(
    override val name: String,
    override val statements: ArrayList<IRStatement>,
    override var parent: IRStatementContainer?,
    override val type: IRType
) : IRStatementContainer, IRExpression{
    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitBlock(this, data)

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            blue {
            }
            appendWithNewLine("do")
            indent{
                statements.forEach {
                    appendWithNewLine(it.toString())
                }
            }
            appendWithNewLine("end")
            blue {
            }
        }
}