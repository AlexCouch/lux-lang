package ir.declarations

import buildPrettyString
import ir.symbol.IRConstSymbol
import ir.symbol.IRVarSymbol
import ir.symbol.IRVarSymbolBase
import ir.types.IRType
import ir.visitors.IRElementVisitor
import ir.visitors.IRElementTransformer

class IRConst(override val name: String,
              override val type: IRType,
              override val expression: IRExpression,
              override var parent: IRStatementContainer?,
              override val symbol: IRVarSymbolBase<IRConst>
) : IRVarDeclaration<IRVarSymbolBase<IRConst>>{
    init{
        symbol.bind(this)
    }

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitConst(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        expression.transform(transformer, data)
    }

    override fun toString(): String =
        buildPrettyString {
            append("$type const %$name = $expression")
        }

}