package ir

import TokenPos
import ir.declarations.IRStatementContainer

interface IRStatement : IRElement{
    val position: TokenPos
    var parent: IRStatementContainer?
}