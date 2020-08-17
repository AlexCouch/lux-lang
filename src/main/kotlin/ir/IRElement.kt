package ir

import ir.visitors.IRElementTransformer
import ir.visitors.IRElementVisitor

interface IRElement{
    fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R

    fun <R, D> acceptChildren(visitor: IRElementVisitor<R, D>, data: D) = accept(visitor, data)

    fun <D> transform(transformer: IRElementTransformer<D>, data: D): IRElement = accept(transformer, data)
    fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D)

    fun toPrettyString(): String
}