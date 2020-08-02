package ir.declarations.expressions

import buildPrettyString
import ir.IRStatement
import ir.declarations.IRStatementContainer
import ir.visitors.IRElementVisitor

class IRBlock(
    override val name: String,
    override val statements: ArrayList<IRStatement>,
    override var parent: IRStatementContainer?
) : IRStatementContainer{
    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitBlock(this, data)

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            blue {
                appendWithNewLine("do")
            }
            statements.forEach {
                append(it.toString())
            }
            blue {
                appendWithNewLine("end")
            }
        }
}