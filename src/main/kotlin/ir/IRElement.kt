package ir

import arrow.core.Either
import errors.SourceAnnotation
import ir.visitors.IRElementTransformer
import ir.visitors.IRElementVisitor

interface IRElement{
    fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): Either<R, SourceAnnotation>

    fun <R, D> acceptChildren(visitor: IRElementVisitor<R, D>, data: D) = accept(visitor, data)

    fun <D> transform(transformer: IRElementTransformer<D>, data: D): Either<IRElement, SourceAnnotation> = accept(transformer, data)
    fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D)

    fun toPrettyString(): String
}