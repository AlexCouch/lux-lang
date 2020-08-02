package ir.declarations.expressions.branching

import ir.declarations.IRExpression
import ir.declarations.expressions.IRBlock

interface IRBranching: IRExpression{
    val condition: IRExpression
    val then: IRBlock
    val otherwise: IRBlock
}