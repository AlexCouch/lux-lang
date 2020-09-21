package ir.visitors

import arrow.core.Either
import errors.SourceAnnotation
import ir.IRElement
import ir.IRStatement
import ir.declarations.*
import ir.declarations.expressions.*
import ir.declarations.expressions.branching.IRBinaryConditional

interface IRElementVisitor<R, D> {
    fun visitElement(element: IRElement, data: D): Either<R, SourceAnnotation>
    fun visitModule(element: IRModule, data: D): Either<R, SourceAnnotation>
    fun visitStatement(element: IRStatement, data: D): Either<R, SourceAnnotation>
    fun visitConst(element: IRConst, data: D): Either<R, SourceAnnotation> = visitStatement(element, data)
    fun visitExpression(element: IRExpression, data: D): Either<R, SourceAnnotation> = visitElement(element, data)
    fun <T> visitConstant(element: IRConstant<T>, data: D): Either<R, SourceAnnotation> = visitExpression(element, data)
    fun visitVar(element: IRVar, data: D): Either<R, SourceAnnotation> = visitStatement(element, data)
    fun visitMutation(element: IRMutation, data: D): Either<R, SourceAnnotation> = visitStatement(element, data)
    fun visitBinary(element: IRBinary, data: D): Either<R, SourceAnnotation> = visitExpression(element, data)
    fun visitBinaryPlus(element: IRBinaryPlus, data: D): Either<R, SourceAnnotation> = visitExpression(element, data)
    fun visitBinaryMinus(element: IRBinaryMinus, data: D): Either<R, SourceAnnotation> = visitExpression(element, data)
    fun visitBinaryMult(element: IRBinaryMult, data: D): Either<R, SourceAnnotation> = visitExpression(element, data)
    fun visitBinaryDiv(element: IRBinaryDiv, data: D): Either<R, SourceAnnotation> = visitExpression(element, data)
    fun visitRef(element: IRRef, data: D): Either<R, SourceAnnotation> = visitExpression(element, data)
    fun visitPrint(element: IRPrint, data: D): Either<R, SourceAnnotation> = visitStatement(element, data)
    fun visitLegacyVar(element: IRLegacyVar, data: D): Either<R, SourceAnnotation> = visitStatement(element, data)
    fun visitProc(element: IRProc, data: D): Either<R, SourceAnnotation> = visitStatement(element, data)
    fun visitProcParam(element: IRProcParam, data: D): Either<R, SourceAnnotation> = visitStatement(element, data)
    fun visitProcCall(element: IRProcCall, data: D): Either<R, SourceAnnotation> = visitExpression(element, data)
    fun visitReturn(element: IRReturn, data: D): Either<R, SourceAnnotation> = visitStatement(element, data)
    fun visitBlock(element: IRBlock, data: D): Either<R, SourceAnnotation> = visitStatement(element, data)
    fun visitBinaryConditional(element: IRBinaryConditional, data: D): Either<R, SourceAnnotation> = visitStatement(element, data)
}