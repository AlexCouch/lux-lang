package parser

interface ASTVisitor<P, R, D>{
    fun visitModule(module: Node.ModuleNode, data: D): R
    fun visitStatement(statement: Node.StatementNode, parent: P, data: D): R
    fun visitExpression(expression: Node.StatementNode.ExpressionNode, parent: P, data: D): R = visitStatement(expression, parent, data)
    fun visitLet(let: Node.StatementNode.LetNode, parent: P, data: D): R = visitStatement(let, parent, data)
    fun visitVar(varNode: Node.StatementNode.VarNode, parent: P, data: D): R = visitVar(varNode, parent, data)
    fun visitConst(constNode: Node.StatementNode.ConstNode, parent: P, data: D): R = visitStatement(constNode, parent, data)
    fun visitProc(procNode: Node.StatementNode.DefProcNode, parent: P, data: D): R = visitStatement(procNode, parent, data)
    fun visitBlock(blockNode: Node.StatementNode.ExpressionNode.BlockNode, parent: P, data: D): R = visitExpression(blockNode, parent, data)
    fun visitProcCall(procCallNode: Node.StatementNode.ExpressionNode.ProcCallNode, parent: P, data: D): R = visitExpression(procCallNode, parent, data)
    fun visitProcParam(procParamNode: Node.StatementNode.ProcParamNode, parent: P, data: D): R = visitStatement(procParamNode, parent, data)
    fun visitIntegerLiteral(intLiteral: Node.StatementNode.ExpressionNode.IntegerLiteralNode, parent: P, data: D): R = visitStatement(intLiteral, parent, data)
    fun visitStringLiteral(strLiteral: Node.StatementNode.ExpressionNode.StringLiteralNode, parent: P, data: D): R = visitStatement(strLiteral, parent, data)
    fun visitBinary(binaryNode: Node.StatementNode.ExpressionNode.BinaryNode, parent: P, data: D): R = visitExpression(binaryNode, parent, data)
    fun visitRef(refNode: Node.StatementNode.ExpressionNode.ReferenceNode, parent: P, data: D): R = visitExpression(refNode, parent, data)
    fun visitMutation(mutationNode: Node.StatementNode.ReassignmentNode, parent: P, data: D): R = visitStatement(mutationNode, parent, data)
    fun visitPrint(print: Node.StatementNode.PrintNode, parent: P, data: D): R = visitStatement(print, parent, data)
    fun visitReturn(ret: Node.StatementNode.ReturnNode, parent: P, data: D): R = visitStatement(ret, parent, data)
    fun visitBinaryConditional(conditional: Node.StatementNode.ExpressionNode.ConditionalBranchingNode.BinaryConditionalNode, parent: P, data: D): R = visitExpression(conditional, parent, data)
}