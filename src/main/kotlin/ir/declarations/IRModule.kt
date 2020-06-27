package ir.declarations

import buildPrettyString
import ir.IRStatement
import ir.visitors.IRElementVisitor

class IRModule(override val name: String, override var parent: IRStatementContainer? = null): IRStatementContainer{

    override val statements: ArrayList<IRStatement> = arrayListOf()

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitModule(this, data)

    override fun toString(): String =
        buildPrettyString{
            appendWithNewLine("mod %$name")
            indent {
                statements.forEach {
                    appendWithNewLine(it.toString())
                }
            }
            append("endmod %$name")
        }

}