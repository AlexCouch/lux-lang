package ir.declarations

import ir.IRStatement
import ir.visitors.IRElementTransformer

interface IRStatementContainer : IRDeclarationWithName{
    val statements: ArrayList<IRStatement>

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        statements.forEach {
            it.transform(transformer, data)
        }
    }
}