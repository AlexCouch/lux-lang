package ir.declarations

import buildPrettyString
import ir.symbol.IRLetSymbol
import ir.symbol.IRVarSymbol
import ir.symbol.IRVarSymbolBase
import ir.types.IRType
import ir.visitors.IRElementTransformer
import ir.visitors.IRElementVisitor

class IRLet(override val name: String,
            override val type: IRType,
            override val expression: IRExpression,
            override var parent: IRStatementContainer?,
            override val symbol: IRVarSymbolBase<IRLet>
) : IRDeclarationWithName, IRVarDeclaration<IRVarSymbolBase<IRLet>>{

    init{
        symbol.bind(this)
    }

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitLet(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        expression.transform(transformer, data)
    }

    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString{
            green {
                append(type.toPrettyString())
            }
            append(" ")
            blue{
                append("let")
            }
            append(" ")
            red{
                append("%$name")
            }
            append(" = ${expression.toPrettyString()}")
        }

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString{
            append("$type")
            append(" ")
            append("let")
            append(" ")
            append("%$name")
            append(" = $expression")
        }

}