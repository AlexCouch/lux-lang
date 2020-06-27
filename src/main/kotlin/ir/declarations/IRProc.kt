package ir.declarations

import buildPrettyString
import ir.IRStatement
import ir.builtin.BuiltinTypes
import ir.types.IRType
import ir.visitors.IRElementVisitor

class IRProc(override val name: String, val returnType: IRType = BuiltinTypes.DYNAMIC.makeSimpleType(), override var parent: IRStatementContainer?) : IRStatementContainer {
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

sealed class IRProcParam(val name: String){
    class IRUntypedProcParam(name: String): IRProcParam(name)
    class IRTypedProcParam(name: String, val type: IRType): IRProcParam(name)
}
