package ir.declarations.expressions.branching

import TokenPos
import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.option.monad.flatMap
import arrow.core.some
import buildPrettyString
import ir.declarations.IRExpression
import ir.declarations.IRStatementContainer
import ir.declarations.expressions.IRBlock
import ir.types.IRType
import ir.visitors.IRElementTransformer
import ir.visitors.IRElementVisitor

class IRBinaryConditional(
    val condition: IRExpression,
    val then: IRBlock,
    val otherwise: Option<IRBlock>,
    override val type: IRType,
    override var parent: IRStatementContainer?,
    override val startPos: TokenPos,
    override val endPos: TokenPos
) : IRExpression {
    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
        visitor.visitBinaryConditional(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        condition.transform(transformer, data)
        then.transform(transformer, data)
        otherwise.map { it.transform(transformer, data) }
    }

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            append("if $condition ")
            append(then.toString())
            if(otherwise is Some){
                append("else ${otherwise.t}")
            }
        }

    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString {
            blue {
                append("if ")
            }
            append(condition.toPrettyString())
            spaced(0)
            append(then.toPrettyString())
            if(otherwise is Some){
                blue {
                    append("else ")
                }
                append(otherwise.t.toPrettyString())
            }
        }
}