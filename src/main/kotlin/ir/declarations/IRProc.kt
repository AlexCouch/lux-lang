package ir.declarations

import TokenPos
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
    override val symbol: IRProcSymbol,
    override val startPos: TokenPos,
    override val endPos: TokenPos
) : IRStatementContainer,
    IRSymbolOwner
{
    init{
        symbol.bind(this)
    }

    override val statements: ArrayList<IRStatement> = arrayListOf()
    val params: ArrayList<IRProcParam> = arrayListOf()

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
        visitor.visitProc(this, data)

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            appendWithNewLine("")
            append("$returnType")
            append(" ")
            append("proc")
            append(" ")
            append("%$name")
            append("(")
            params.forEachIndexed { i, it ->
                append("${it.type}")

                append(" ")
                append("%${it.name}")
                if(i < params.size - 1) append(", ")
            }
            append(") ")
            appendWithNewLine("do")
            indent {
                statements.forEach {
                    appendWithNewLine(it.toString())
                }
            }
            append("endproc")
            append(" ")
            appendWithNewLine("%$name")
        }
    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString {
            appendWithNewLine("")
            green {
                append("$returnType")
            }
            append(" ")
            blue {
                append("proc")
            }
            append(" ")
            red {
                append("%$name")
            }
            append("(")
            params.forEachIndexed { i, it ->
                append(it.toPrettyString())
                if(i < params.size - 1) append(", ")
            }
            append(") ")
            blue {
                appendWithNewLine("do")
            }
            indent {
                statements.forEach {
                    appendWithNewLine(it.toPrettyString())
                }
            }
            blue{
                append("endproc")
            }
            append(" ")
            red {
                appendWithNewLine("%$name")
            }
        }
}

class IRProcParam(
    val name: String,
    override var parent: IRStatementContainer?,
    val type: IRType = IRType.default,
    override val symbol: IRProcParamSymbol,
    override val startPos: TokenPos,
    override val endPos: TokenPos
):
    IRStatement,
    IRSymbolOwner,
    IRBindableSymbolBase<IRProcParam>() {

    init{
        symbol.bind(this)
    }

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
        visitor.visitProcParam(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        //No children
    }

    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString {
            green{
                append(type.toPrettyString())
            }
            append(" ")
            red {
                append("%${name}")
            }
        }
}
