package passes.symbolResolution

import ASTVisitor
import Node
import ir.IRElement
import ir.IRStatement
import ir.declarations.*
import ir.declarations.expressions.*
import ir.symbol.IRConstSymbol
import ir.types.IRType

class SymbolResolutionPass: ASTVisitor<IRStatementContainer, IRElement, SymbolTable>{
    override fun visitModule(module: Node.ModuleNode, data: SymbolTable): IRModule{
        val irModule = data.declareModule(module.ident.str)
        data.enterScope(irModule)
        module.statements.forEach {
            irModule.statements += visitStatement(it, irModule, data)
        }
        data.forEachReference {
            val refIdent = it.owner.refName
            assert(data.hasVariable(refIdent)){
                "$refIdent is not a valid variable symbol"
            }
        }
        data.forEachMutation {
            val mutIdent = it.owner.name
            assert(data.hasVariable(mutIdent)){
                "$mutIdent is not a valid variable symbol"
            }
            val findConst = data.referenceConst(mutIdent)
            assert(findConst == null){
                "$mutIdent is declared const and cannot mutate"
            }
        }
        data.forEachProcCall {
            val procIdent = it.owner.name
            assert(data.findProc(procIdent) != null){
                "$procIdent is not a valid procedure symbol"
            }
        }
        data.leaveScope(irModule)
        return irModule
    }

    override fun visitStatement(statement: Node.StatementNode, parent: IRStatementContainer, data: SymbolTable): IRStatement =
        when(statement){
            is Node.StatementNode.ConstNode -> visitConst(statement, parent, data)
            is Node.StatementNode.VarNode -> visitVar(statement, parent, data)
            is Node.StatementNode.ReassignmentNode -> visitMutation(statement, parent, data)
            is Node.StatementNode.PrintNode -> visitPrint(statement, parent, data)
            is Node.StatementNode.DefProcNode -> visitProc(statement, parent, data)
            is Node.StatementNode.LetNode -> visitLet(statement, parent, data)
            is Node.StatementNode.ExpressionNode.ProcCallNode -> visitProcCall(statement, parent, data)
            else -> TODO("Working on it")
        }

    override fun visitProc(procNode: Node.StatementNode.DefProcNode, parent: IRStatementContainer, data: SymbolTable): IRProc{
        val irProc = data.declareProc(procNode.ident.str, IRType.default, parent)
        data.enterScope(irProc)
        irProc.params.addAll(procNode.params.map {
            visitProcParam(it, irProc, data)
        })
        irProc.statements.addAll(procNode.body.map {
            visitStatement(it, irProc, data)
        })
        data.leaveScope(irProc)
        return irProc
    }

    override fun visitProcParam(procParamNode: Node.StatementNode.ProcParamNode, parent: IRStatementContainer, data: SymbolTable): IRProcParam =
        IRProcParam.IRUntypedProcParam(procParamNode.ident.str, parent)

    override fun visitPrint(print: Node.StatementNode.PrintNode, parent: IRStatementContainer, data: SymbolTable): IRPrint =
        IRPrint(visitExpression(print.expr, parent, data), parent)

    override fun visitConst(constNode: Node.StatementNode.ConstNode, parent: IRStatementContainer, data: SymbolTable): IRConst{
        val expr = visitExpression(constNode.expression, parent, data)
        return data.declareConst(constNode.identifier.str, expr.type, expr, parent)
    }

    override fun visitLet(let: Node.StatementNode.LetNode, parent: IRStatementContainer, data: SymbolTable): IRLet{
        val expr = visitExpression(let.expression, parent, data)
        return data.declareLet(let.identifier.str, expr.type, expr, parent)
    }

    override fun visitVar(varNode: Node.StatementNode.VarNode, parent: IRStatementContainer, data: SymbolTable): IRVar{
        val expr = visitExpression(varNode.expression, parent, data)
        return data.declareVariable(varNode.identifier.str, expr.type, expr, parent)
    }

    override fun visitMutation(mutationNode: Node.StatementNode.ReassignmentNode, parent: IRStatementContainer, data: SymbolTable): IRMutation{
        val expr = visitExpression(mutationNode.expr, parent, data)
        return data.declareMutation(mutationNode.ident.str, parent, expr)
    }

    override fun visitExpression(expression: Node.StatementNode.ExpressionNode, parent: IRStatementContainer, data: SymbolTable): IRExpression =
        when(expression){
            is Node.StatementNode.ExpressionNode.IntegerLiteralNode -> visitIntegerLiteral(expression, parent, data)
            is Node.StatementNode.ExpressionNode.BinaryNode -> visitBinary(expression, parent, data)
            is Node.StatementNode.ExpressionNode.ReferenceNode -> visitRef(expression, parent, data)
            is Node.StatementNode.ExpressionNode.ProcCallNode -> visitProcCall(expression, parent, data)
            is Node.StatementNode.ExpressionNode.BlockNode -> visitBlock(expression, parent, data)
        }

    override fun visitProcCall(procCallNode: Node.StatementNode.ExpressionNode.ProcCallNode, parent: IRStatementContainer, data: SymbolTable): IRExpression =
        data.declareProcCall(procCallNode.refIdent.str, procCallNode.arguments.map { visitExpression(it, parent, data) }.toMutableList() as ArrayList<IRExpression>, parent)

    override fun visitRef(refNode: Node.StatementNode.ExpressionNode.ReferenceNode, parent: IRStatementContainer, data: SymbolTable): IRRef =
        data.declareReference(refNode.refIdent.str, parent)

    override fun visitBinary(binaryNode: Node.StatementNode.ExpressionNode.BinaryNode, parent: IRStatementContainer, data: SymbolTable): IRBinary{
        val left = visitExpression(binaryNode.left, parent, data)
        val right = visitExpression(binaryNode.right, parent, data)

        return when(binaryNode){
            is Node.StatementNode.ExpressionNode.BinaryNode.BinaryAddNode -> IRBinaryPlus(left, right, left.type, parent)
            is Node.StatementNode.ExpressionNode.BinaryNode.BinaryMinusNode -> IRBinaryMinus(left, right, left.type, parent)
            is Node.StatementNode.ExpressionNode.BinaryNode.BinaryMultNode -> IRBinaryMult(left, right, left.type, parent)
            is Node.StatementNode.ExpressionNode.BinaryNode.BinaryDivNode -> IRBinaryDiv(left, right, left.type, parent)
        }
    }

    override fun visitIntegerLiteral(intLiteral: Node.StatementNode.ExpressionNode.IntegerLiteralNode, parent: IRStatementContainer, data: SymbolTable): IRConstant<Int> =
        IRConstant.integer(intLiteral.int, parent)

    override fun visitBlock(
        blockNode: Node.StatementNode.ExpressionNode.BlockNode,
        parent: IRStatementContainer,
        data: SymbolTable
    ): IRExpression {
        TODO("Not yet implemented")
    }

}