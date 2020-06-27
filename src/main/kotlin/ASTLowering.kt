import ir.IRStatement
import ir.declarations.*
import ir.declarations.expressions.*
import ir.types.IRType

class ASTLowering{
    fun visitModule(module: Node.ModuleNode): IRModule{
        val irModule = IRModule(module.ident.str)
        module.statements.forEach {
            irModule.statements += visitStatement(it, irModule)
        }
        return irModule
    }

    fun visitStatement(statement: Node.StatementNode, statementContainer: IRStatementContainer? = null): IRStatement =
        when(statement){
            is Node.StatementNode.ConstNode -> visitConst(statement, statementContainer)
            is Node.StatementNode.VarNode -> visitVar(statement, statementContainer)
            is Node.StatementNode.ReassignmentNode -> visitMutation(statement, statementContainer)
            is Node.StatementNode.PrintNode -> visitPrint(statement, statementContainer)
            is Node.StatementNode.DefProcNode -> visitProc(statement, statementContainer)
            is Node.StatementNode.LetNode -> visitLet(statement, statementContainer)
            is Node.StatementNode.ExpressionNode.ProcCallNode -> visitProcCall(statement, statementContainer)
            else -> TODO("Working on it")
        }

    fun visitProc(proc: Node.StatementNode.DefProcNode, statementContainer: IRStatementContainer? = null): IRProc{
        val irProc = IRProc(proc.ident.str, parent = statementContainer)
        irProc.params.addAll(proc.params.map {
            visitProcParam(it)
        })
        irProc.statements.addAll(proc.body.map {
            visitStatement(it)
        })
        return irProc
    }

    fun visitProcParam(param: Node.StatementNode.ProcParamNode, statementContainer: IRStatementContainer? = null): IRProcParam =
        IRProcParam.IRUntypedProcParam(param.ident.str)

    fun visitPrint(print: Node.StatementNode.PrintNode, statementContainer: IRStatementContainer? = null): IRPrint =
        IRPrint(visitExpression(print.expr), statementContainer)

    fun visitConst(const: Node.StatementNode.ConstNode, statementContainer: IRStatementContainer? = null): IRConst{
        val expr = visitExpression(const.expression, statementContainer)
        return IRConst(const.identifier.str, expr, statementContainer)
    }

    fun visitLet(const: Node.StatementNode.LetNode, statementContainer: IRStatementContainer? = null): IRLet{
        val expr = visitExpression(const.expression, statementContainer)
        return IRLet(const.identifier.str, expr, statementContainer)
    }

    fun visitVar(const: Node.StatementNode.VarNode, statementContainer: IRStatementContainer? = null): IRConst{
        val expr = visitExpression(const.expression, statementContainer)
        return IRConst(const.identifier.str, expr, statementContainer)
    }

    fun visitMutation(mutation: Node.StatementNode.ReassignmentNode, statementContainer: IRStatementContainer? = null): IRMutation{
        val expr = visitExpression(mutation.expr, statementContainer)
        return IRMutation(mutation.ident.str, statementContainer, expr)
    }

    fun visitExpression(expr: Node.StatementNode.ExpressionNode, statementContainer: IRStatementContainer? = null): IRExpression =
        when(expr){
            is Node.StatementNode.ExpressionNode.IntegerLiteralNode -> visitIntegerLiteral(expr, statementContainer)
            is Node.StatementNode.ExpressionNode.BinaryNode -> visitBinary(expr, statementContainer)
            is Node.StatementNode.ExpressionNode.ReferenceNode -> visitRef(expr, statementContainer)
            is Node.StatementNode.ExpressionNode.ProcCallNode -> visitProcCall(expr, statementContainer)
        }

    fun visitProcCall(procCallNode: Node.StatementNode.ExpressionNode.ProcCallNode, statementContainer: IRStatementContainer? = null): IRExpression =
        IRProcCall(procCallNode.refIdent.str, procCallNode.arguments.map { visitExpression(it) }.toMutableList() as ArrayList<IRExpression>)

    fun visitRef(ref: Node.StatementNode.ExpressionNode.ReferenceNode, statementContainer: IRStatementContainer? = null): IRRef =
        IRRef(ref.refIdent.str, IRType.default)

    fun visitBinary(binary: Node.StatementNode.ExpressionNode.BinaryNode, statementContainer: IRStatementContainer? = null): IRBinary{
        val left = visitExpression(binary.left)
        val right = visitExpression(binary.right)

        return when(binary){
            is Node.StatementNode.ExpressionNode.BinaryNode.BinaryAddNode -> IRBinaryPlus(left, right, left.type)
            is Node.StatementNode.ExpressionNode.BinaryNode.BinaryMinusNode -> IRBinaryMinus(left, right, left.type)
            is Node.StatementNode.ExpressionNode.BinaryNode.BinaryMultNode -> IRBinaryMult(left, right, left.type)
            is Node.StatementNode.ExpressionNode.BinaryNode.BinaryDivNode -> IRBinaryDiv(left, right, left.type)
        }
    }

    fun visitIntegerLiteral(lit: Node.StatementNode.ExpressionNode.IntegerLiteralNode, statementContainer: IRStatementContainer? = null): IRConstant<Int> =
        IRConstant.integer(lit.int)

}