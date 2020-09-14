package ir.visitors

import arrow.core.Either
import arrow.core.left
import errors.SourceAnnotation
import ir.IRElement
import ir.IRStatement
import ir.declarations.IRModule

interface IRElementTransformer<D> : IRElementVisitor<IRElement, D> {
    override fun visitElement(element: IRElement, data: D): Either<IRElement, SourceAnnotation> =
        element.also { it.transformChildren(this, data) }.left()

    override fun visitModule(element: IRModule, data: D): Either<IRElement, SourceAnnotation> =
        element.also { it.transformChildren(this, data) }.left()

    override fun visitStatement(element: IRStatement, data: D): Either<IRElement, SourceAnnotation> =
        element.also { it.transformChildren(this, data) }.left()

}