package ir

import TokenPos
import ir.declarations.IRStatementContainer

interface IRStatement : IRElement{

    var parent: IRStatementContainer?
}