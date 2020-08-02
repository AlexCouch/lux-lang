package ir.declarations.ssa

import ir.IRElement
import ir.declarations.IRExpression

interface SSAElement: IRElement{
    val variables: List<SSAVar>
    val expressions: List<IRExpression>
    val children: List<IRElement>
}