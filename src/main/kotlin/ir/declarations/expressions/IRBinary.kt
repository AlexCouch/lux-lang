package ir.declarations.expressions

import buildPrettyString
import ir.visitors.IRElementVisitor
import ir.declarations.IRExpression
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
    override val type: IRType
): IRBinary{
    override val kind: IRBinaryKind = IRBinaryKind.PLUS
    override val precendence: Int = 2

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitBinaryPlus(this, data)

    override fun toString(): String =
        buildPrettyString {
            append("add $left, $right")
        }
}

class IRBinaryMinus(
    override val left: IRExpression,
    override val right: IRExpression,
    override val type: IRType
): IRBinary{
    override val kind: IRBinaryKind = IRBinaryKind.MINUS
    override val precendence: Int = 2

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitBinaryMinus(this, data)

    override fun toString(): String =
        buildPrettyString {
            append("sub $left, $right")
        }
}

class IRBinaryMult(
    override val left: IRExpression,
    override val right: IRExpression,
    override val type: IRType
): IRBinary{
    override val kind: IRBinaryKind = IRBinaryKind.MULT
    override val precendence: Int = 1

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitBinaryMult(this, data)

    override fun toString(): String =
        buildPrettyString {
            append("mul $left, $right")
        }
}

class IRBinaryDiv(
    override val left: IRExpression,
    override val right: IRExpression,
    override val type: IRType
): IRBinary{
    override val kind: IRBinaryKind = IRBinaryKind.DIV
    override val precendence: Int = 1

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitBinaryDiv(this, data)

    override fun toString(): String =
        buildPrettyString {
            append("div $left, $right")
        }
}