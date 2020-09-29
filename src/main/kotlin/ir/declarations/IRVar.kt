package ir.declarations

import TokenPos
import buildPrettyString
import ir.symbol.IRVarSymbolBase
import ir.types.IRType
import ir.visitors.IRElementVisitor
import ir.visitors.IRElementTransformer

class IRVar(
    override val name: String,
    override val type: IRType,
    override val expression: IRExpression,
    override var parent: IRStatementContainer?,
    override val symbol: IRVarSymbolBase<IRVar>,
    override val startPos: TokenPos,
    override val endPos: TokenPos
) : IRVarDeclaration<IRVarSymbolBase<IRVar>>{
    init{
        symbol.bind(this)
    }

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
        visitor.visitVar(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        expression.transform(transformer, data)
    }

    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString {
            green {
                append("$type")
            }
            append(" ")
            blue{
                append("var")
            }
            append(" ")
            red{
                append("%$name")
            }
            append(" = ${expression.toPrettyString()}")
        }
    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            append("$type")
            append(" ")
            append("var")
            append(" ")
            append("%$name")
            append(" = $expression")
        }
}