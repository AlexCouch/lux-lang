package ir.declarations

import ir.IRStatement

interface IRDeclaration : IRStatement{

}

interface IRDeclarationWithName : IRDeclaration{
    val name: String
}