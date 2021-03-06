package ir.declarations.expressions

import TokenPos
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
    override val type: IRType,
    override val startPos: TokenPos,
    override val endPos: TokenPos
) : IRStatementContainer, IRExpression{
    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
        visitor.visitBlock(this, data)

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            appendWithNewLine("do")
            indent{
                statements.forEach {
                    append(it.toString())
                }
            }
            appendWithNewLine("end")
        }

    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString {
            blue {
                appendWithNewLine("do")
            }
            indent{
                statements.forEach {
                    appendWithNewLine(it.toPrettyString())
                }
            }
            blue {
                appendWithNewLine("end")
            }
        }
}