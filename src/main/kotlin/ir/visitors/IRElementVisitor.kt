package ir.visitors

import ir.IRElement
import ir.IRStatement
import ir.declarations.*
import ir.declarations.expressions.*

interface IRElementVisitor<R, D> {
    fun visitElement(element: IRElement, data: D): R
    fun visitModule(element: IRModule, data: D): R
    fun visitStatement(element: IRStatement, data: D): R
    fun visitConst(element: IRConst, data: D): R = visitStatement(element, data)
    fun visitExpression(element: IRExpression, data: D): R = visitElement(element, data)
    fun <T> visitConstant(element: IRConstant<T>, data: D): R = visitExpression(element, data)
    fun visitVar(element: IRVar, data: D): R = visitStatement(element, data)
    fun visitMutation(element: IRMutation, data: D): R = visitStatement(element, data)
    fun visitBinary(element: IRBinary, data: D): R = visitExpression(element, data)
    fun visitBinaryPlus(element: IRBinaryPlus, data: D): R = visitExpression(element, data)
    fun visitBinaryMinus(element: IRBinaryMinus, data: D): R = visitExpression(element, data)
    fun visitBinaryMult(element: IRBinaryMult, data: D): R = visitExpression(element, data)
    fun visitBinaryDiv(element: IRBinaryDiv, data: D): R = visitExpression(element, data)
    fun visitRef(element: IRRef, data: D): R = visitExpression(element, data)
    fun visitPrint(element: IRPrint, data: D): R = visitStatement(element, data)
    fun visitLet(element: IRLet, data: D): R = visitStatement(element, data)
    fun visitProc(element: IRProc, data: D): R = visitStatement(element, data)
    fun visitProcParam(element: IRProcParam, data: D): R = visitStatement(element, data)
    fun visitProcCall(element: IRProcCall, data: D): R = visitExpression(element, data)
}