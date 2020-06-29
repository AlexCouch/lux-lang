package ir.declarations

import buildPrettyString
import ir.IRStatement
import ir.builtin.BuiltinTypes
import ir.symbol.IRProcSymbol
import ir.symbol.IRSymbol
import ir.symbol.IRSymbolOwner
import ir.types.IRType
import ir.visitors.IRElementTransformer
import ir.visitors.IRElementVisitor

class IRProc(
    override val name: String,
    val returnType: IRType = BuiltinTypes.DYNAMIC.makeSimpleType(),
    override var parent: IRStatementContainer?,
    override val symbol: IRProcSymbol
) : IRStatementContainer,
    IRSymbolOwner
{
    init{
        symbol.bind(this)
    }

    override val statements: ArrayList<IRStatement> = arrayListOf()
    val params: ArrayList<IRProcParam> = arrayListOf()

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitProc(this, data)

    override fun toString(): String =
        buildPrettyString {
            appendWithNewLine("")
            append("$returnType proc %$name(")
            params.forEachIndexed { i, it ->
                append("${if(it is IRProcParam.IRTypedProcParam) "${it.type} " else ""}%${it.name}")
                if(i < params.size - 1) append(", ")
            }
            appendWithNewLine(") do")
            indent {
                statements.forEach {
                    appendWithNewLine(it.toString())
                }
            }
            appendWithNewLine("endproc %$name")
        }
}

sealed class IRProcParam(val name: String, override var parent: IRStatementContainer?): IRStatement{
    class IRUntypedProcParam(name: String, override var parent: IRStatementContainer?): IRProcParam(name, parent)
    class IRTypedProcParam(name: String, val type: IRType, override var parent: IRStatementContainer?): IRProcParam(name, parent)

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitProcParam(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        //No children
    }
}
