package ir.declarations.expressions

import TokenPos
import buildPrettyString
import ir.visitors.IRElementVisitor
import ir.declarations.IRExpression
import ir.declarations.IRStatementContainer
import ir.types.IRType
import ir.visitors.IRElementTransformer

interface IRBinary: IROperator{
    val kind: IRBinaryKind
    val left: IRExpression
    val right: IRExpression

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        left.transform(transformer, data)
        right.transform(transformer, data)
    }
}

enum class IRBinaryKind{
    PLUS,
    MINUS,
    MULT,
    DIV
}

class IRBinaryPlus(
    override val left: IRExpression,
    override val right: IRExpression,
    override val type: IRType,
    override var parent: IRStatementContainer?,
    override val position: TokenPos
): IRBinary{
    override val kind: IRBinaryKind = IRBinaryKind.PLUS
    override val precendence: Int = 2

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
        visitor.visitBinaryPlus(this, data)

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            append("add")
            append(" $left, $right")
        }
    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString {
            blue{
                append("add")
            }
            append(" ${left.toPrettyString()}, ${right.toPrettyString()}")
        }
}

class IRBinaryMinus(
    override val left: IRExpression,
    override val right: IRExpression,
    override val type: IRType,
    override var parent: IRStatementContainer?,
    override val position: TokenPos
): IRBinary{
    override val kind: IRBinaryKind = IRBinaryKind.MINUS
    override val precendence: Int = 2

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
        visitor.visitBinaryMinus(this, data)

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            append("sub")
            append(" $left, $right")
        }
    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString {
            blue{
                append("sub")
            }
            append(" ${left.toPrettyString()}, ${right.toPrettyString()}")
        }
}

class IRBinaryMult(
    override val left: IRExpression,
    override val right: IRExpression,
    override val type: IRType,
    override var parent: IRStatementContainer?,
    override val position: TokenPos
): IRBinary{
    override val kind: IRBinaryKind = IRBinaryKind.MULT
    override val precendence: Int = 1

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
        visitor.visitBinaryMult(this, data)

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            append("mul")
            append(" $left, $right")
        }
    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString {
            blue{
                append("mul")
            }
            append(" ${left.toPrettyString()}, ${right.toPrettyString()}")
        }
}

class IRBinaryDiv(
    override val left: IRExpression,
    override val right: IRExpression,
    override val type: IRType,
    override var parent: IRStatementContainer?,
    override val position: TokenPos
): IRBinary{
    override val kind: IRBinaryKind = IRBinaryKind.DIV
    override val precendence: Int = 1

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
        visitor.visitBinaryDiv(this, data)

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            append("div")
            append(" $left, $right")
        }
    @ExperimentalStdlibApi
    override fun toPrettyString(): String =
        buildPrettyString {
            blue{
                append("div")
            }
            append(" ${left.toPrettyString()}, ${right.toPrettyString()}")
        }
}