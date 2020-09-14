package ir.declarations.expressions

import TokenPos
import buildPrettyString
import ir.visitors.IRElementVisitor
import ir.builtin.BuiltinTypes
import ir.declarations.IRExpression
import ir.declarations.IRStatementContainer
import ir.types.IRType
import ir.visitors.IRElementTransformer

class IRConstant<T>(
    override val type: IRType,
    val kind: IRConstantKind<T>,
    val value: T,
    override var parent: IRStatementContainer?,
    override val position: TokenPos
):
    IRExpression {
    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D) =
        visitor.visitConstant(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        //No children
    }

    companion object{
        fun integer(int: Int, parent: IRStatementContainer?, position: TokenPos) = IRConstant(
            BuiltinTypes.INT.makeSimpleType(),
            IRConstantKind.Int,
            int,
            parent,
            position
        )
        fun string(str: String, parent: IRStatementContainer?, position: TokenPos) = IRConstant(
            BuiltinTypes.STR.makeSimpleType(),
            IRConstantKind.Str,
            str,
            parent,
            position
        )
    }

    @ExperimentalStdlibApi
    override fun toString(): String  =
        buildPrettyString{
            when(kind){
                is IRConstantKind.Int -> {
                    append("int $value")
                }
                is IRConstantKind.Str -> {
                    append("str $value")
                }
            }
        }

    @ExperimentalStdlibApi
    override fun toPrettyString(): String  =
        buildPrettyString{
            when(kind){
                is IRConstantKind.Int -> {
                    green{
                        append("int ")
                    }
                    blue {
                        append("$value")
                    }
                }
                is IRConstantKind.Str -> {
                    green{
                        append("str ")
                    }
                    yellow {
                        append("\"$value\"")
                    }
                }
            }
        }

}

sealed class IRConstantKind<T>(val asString: String){
    object Int : IRConstantKind<kotlin.Int>("Int")
    object Str : IRConstantKind<String>("String")
}