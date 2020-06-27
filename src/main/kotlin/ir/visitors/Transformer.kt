package ir.visitors

import ir.IRElement
import ir.IRStatement
import ir.declarations.IRModule

interface IRElementTransformer<D> : IRElementVisitor<IRElement, D> {
    override fun visitElement(element: IRElement, data: D): IRElement =
        element.also { it.transformChildren(this, data) }

    override fun visitModule(element: IRModule, data: D): IRElement =
        element.also { it.transformChildren(this, data) }

    override fun visitStatement(element: IRStatement, data: D): IRElement =
        element.also { it.transformChildren(this, data) }

}