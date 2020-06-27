package ir.declarations.expressions

import ir.declarations.IRExpression

interface IROperator : IRExpression{
    val precendence: Int
}