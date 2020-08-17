package ir.declarations

import buildPrettyString
import ir.IRStatement
import ir.symbol.IRSymbol
import ir.symbol.IRSymbolOwner
import ir.visitors.IRElementVisitor

class IRModule(override val name: String, override var parent: IRStatementContainer? = null,
               override val symbol: IRSymbol): IRStatementContainer, IRSymbolOwner{

    override val statements: ArrayList<IRStatement> = arrayListOf()

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitModule(this, data)

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString{
            append("mod")
            append(" ")
            appendWithNewLine("%$name")
            indent {
                statements.forEach {
                    appendWithNewLine(it.toString())
                }
            }
            append("endmod")
            append(" %$name")
        }
    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString{
            blue{
                append("mod")
            }
            append(" ")
            red{
                appendWithNewLine("%$name")
            }
            indent {
                statements.forEach {
                    appendWithNewLine(it.toPrettyString())
                }
            }
            blue {
                append("endmod")
            }
            red{
                append(" %$name")
            }
        }
}