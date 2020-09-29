package ir

import ir.declarations.IRStatementContainer

interface IRStatement : IRElement{

    var parent: IRStatementContainer?
}