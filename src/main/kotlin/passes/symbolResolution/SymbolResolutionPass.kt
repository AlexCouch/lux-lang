package passes.symbolResolution

import parser.ASTVisitor
import Node
import arrow.core.extensions.option.monad.flatMap
import com.sun.org.apache.xpath.internal.ExpressionNode
import ir.IRElement
import ir.IRStatement
import ir.declarations.*
import ir.declarations.expressions.*
import ir.declarations.expressions.branching.IRBinaryConditional
import ir.symbol.IRConstSymbol
import ir.types.IRSimpleType
import ir.types.IRType
import passes.TemporaryNameCreator

class SymbolResolutionPass: ASTVisitor<IRStatementContainer, IRElement, SymbolTable>{
    val blockNameGen = TemporaryNameCreator()

    override fun visitModule(module: Node.ModuleNode, data: SymbolTable): IRModule{
        val irModule = data.declareModule(module.ident.str)
        data.enterScope(irModule)
        module.statements.forEach {
            irModule.statements += visitStatement(it, irModule, data)
        }
        data.forEachReference {
            val refIdent = it.owner?.refName!!
            assert(data.hasVariable(refIdent)){
                "$refIdent is not a valid variable symbol"
            }
        }
        data.forEachMutation {
            val mutIdent = it.owner!!.name
            assert(data.hasVariable(mutIdent)){
                "$mutIdent is not a valid variable symbol"
            }
            val findConst = data.referenceConst(mutIdent)
            assert(findConst == null){
                "$mutIdent is declared const and cannot mutate"
            }
        }
        data.forEachProcCall {
            val procIdent = it.owner!!.name
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
            is Node.StatementNode.ReturnNode -> visitReturn(statement, parent, data)
            is Node.StatementNode.ExpressionNode -> visitExpression(statement, parent, data)
            else -> TODO("Working on it")
        }

    override fun visitReturn(
        ret: Node.StatementNode.ReturnNode,
        parent: IRStatementContainer,
        data: SymbolTable
    ): IRReturn {
        val expr = visitExpression(ret.expr, parent, data)
        return IRReturn(expr, parent)
    }

    override fun visitProc(procNode: Node.StatementNode.DefProcNode, parent: IRStatementContainer, data: SymbolTable): IRProc{
        val irProc = data.declareProc(procNode.ident.str, IRSimpleType(procNode.returnType.str), parent)
        data.enterScope(irProc)
        procNode.params.forEach {
            visitProcParam(it, irProc, data)
        }
        irProc.statements.addAll(procNode.body.map {
            visitStatement(it, irProc, data)
        })
        data.leaveScope(irProc)
        return irProc
    }

    override fun visitProcParam(procParamNode: Node.StatementNode.ProcParamNode, parent: IRStatementContainer, data: SymbolTable): IRProcParam =
        data.declareProcParam(parent.name, procParamNode.ident.str, IRSimpleType(procParamNode.type.str))

    override fun visitPrint(print: Node.StatementNode.PrintNode, parent: IRStatementContainer, data: SymbolTable): IRPrint =
        IRPrint(visitExpression(print.expr, parent, data), parent)

    override fun visitConst(constNode: Node.StatementNode.ConstNode, parent: IRStatementContainer, data: SymbolTable): IRConst{
        val expr = visitExpression(constNode.expression, parent, data)
        return data.declareConst(constNode.identifier.str, IRSimpleType(constNode.type.str), expr, parent)
    }

    override fun visitLet(let: Node.StatementNode.LetNode, parent: IRStatementContainer, data: SymbolTable): IRLet{
        val expr = visitExpression(let.expression, parent, data)
        return data.declareLet(let.identifier.str, IRSimpleType(let.type.str), expr, parent)
    }

    override fun visitVar(varNode: Node.StatementNode.VarNode, parent: IRStatementContainer, data: SymbolTable): IRVar{
        val expr = visitExpression(varNode.expression, parent, data)
        return data.declareVariable(varNode.identifier.str, IRSimpleType(varNode.type.str), expr, parent)
    }

    override fun visitMutation(mutationNode: Node.StatementNode.ReassignmentNode, parent: IRStatementContainer, data: SymbolTable): IRMutation{
        val expr = visitExpression(mutationNode.expr, parent, data)
        return data.declareMutation(mutationNode.ident.str, parent, expr)
    }

    override fun visitBinaryConditional(
        conditional: Node.StatementNode.ExpressionNode.ConditionalBranchingNode.BinaryConditionalNode,
        parent: IRStatementContainer,
        data: SymbolTable
    ): IRBinaryConditional {
        val condition = conditional.condition
        val then = conditional.then
        val otherwise = conditional.otherwise

        return IRBinaryConditional(
            visitExpression(condition, parent, data),
            visitBlock(then, parent, data),
            otherwise.map { visitBlock(it, parent, data) },
            IRType.default,
            parent
        )
    }

    override fun visitExpression(expression: Node.StatementNode.ExpressionNode, parent: IRStatementContainer, data: SymbolTable): IRExpression =
        when(expression){
            is Node.StatementNode.ExpressionNode.IntegerLiteralNode -> visitIntegerLiteral(expression, parent, data)
            is Node.StatementNode.ExpressionNode.BinaryNode -> visitBinary(expression, parent, data)
            is Node.StatementNode.ExpressionNode.ReferenceNode -> visitRef(expression, parent, data)
            is Node.StatementNode.ExpressionNode.ProcCallNode -> visitProcCall(expression, parent, data)
            is Node.StatementNode.ExpressionNode.BlockNode -> visitBlock(expression, parent, data)
            is Node.StatementNode.ExpressionNode.ConditionalBranchingNode.BinaryConditionalNode -> visitBinaryConditional(expression, parent, data)
            is Node.StatementNode.ExpressionNode.StringLiteralNode -> visitStringLiteral(expression, parent, data)
            else -> TODO()
        }

    override fun visitStringLiteral(
        strLiteral: Node.StatementNode.ExpressionNode.StringLiteralNode,
        parent: IRStatementContainer,
        data: SymbolTable
    ): IRConstant<String> =
        IRConstant.string(strLiteral.value, parent)

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
    ): IRBlock {
        val block = IRBlock(blockNameGen.name, arrayListOf(), parent, IRType.default)
        block.statements.addAll(blockNode.stmts.map {
            visitStatement(it, block, data)
        })
        return block
    }

}