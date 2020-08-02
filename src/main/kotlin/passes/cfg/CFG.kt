package passes.cfg

import ir.IRElement
import ir.IRStatement
import ir.declarations.*
import ir.visitors.IRElementVisitor

class CFGPass: IRElementVisitor<IRElement, CFG>{
    override fun visitElement(element: IRElement, data: CFG): IRElement =
        when(element){
            is IRModule -> visitModule(element, data)
            is IRStatement -> visitStatement(element, data)
            else -> throw IllegalArgumentException("Excepted either a module or a statement")
        }

    override fun visitModule(element: IRModule, data: CFG): IRElement {
        data.addBasicBlock(element.name, element.statements.filter {
            (it is IRExpression || it is IRDeclaration) && it !is IRStatementContainer
        } as ArrayList<IRStatement>)
        element.statements.forEach {
            when(it){
                is IRStatementContainer -> when(it){
                    is IRProc -> visitProc(it, data)
                }
            }
        }
        return element
    }

    override fun visitProc(element: IRProc, data: CFG): IRElement{
        data.addBasicBlock(element.name, element.statements.filter {
            (it is IRExpression || it is IRDeclaration) && it !is IRStatementContainer
        } as ArrayList<IRStatement>)
        return element
    }

    override fun visitStatement(element: IRStatement, data: CFG): IRElement =
        when(element){
            is IRModule -> visitModule(element, data)
            is IRVar -> visitVar(element, data)
            is IRConst -> visitConst(element, data)
            is IRLet -> visitLet(element, data)
            is IRExpression -> visitExpression(element, data)
            else -> throw IllegalArgumentException("Expected a statement but instead found $element")
        }

}