package ir.declarations

import ir.IRStatement

interface IRDeclaration : IRStatement{
    var parent: IRStatementContainer?
}

interface IRDeclarationWithName : IRDeclaration{
    val name: String
}