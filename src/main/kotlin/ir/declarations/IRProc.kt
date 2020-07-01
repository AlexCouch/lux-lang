package ir.declarations

import buildPrettyString
import ir.IRStatement
import ir.builtin.BuiltinTypes
import ir.symbol.*
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
                append("${it.type} %${it.name}")
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

class IRProcParam(
    val name: String,
    override var parent: IRStatementContainer?,
    val type: IRType = IRType.default,
    override val symbol: IRProcParamSymbol
):
    IRStatement,
    IRSymbolOwner,
    IRBindableSymbolBase<IRProcParam>() {

    init{
        symbol.bind(this)
    }

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitProcParam(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        //No children
    }
}
