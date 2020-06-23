import kotlin.concurrent.thread

class Parser{
    fun parsePrint(token: Token, stream: TokenStream): Node.StatementNode.PrintNode{
        val expr = parseExpression(stream)
        return Node.StatementNode.PrintNode(expr, token.pos)
    }

    fun tryParseBinary(startExpression: Node.StatementNode.ExpressionNode, stream: TokenStream): Node.StatementNode.ExpressionNode.BinaryNode? {
        return when (val peek = stream.peek) {
            is Token.PlusToken -> {
                stream.next()
                val right = parseExpression(stream)
                Node.StatementNode.ExpressionNode.BinaryNode.BinaryAddNode(startExpression, right, peek.pos)
            }
            is Token.MinusToken -> {
                stream.next()
                val right = parseExpression(stream)
                Node.StatementNode.ExpressionNode.BinaryNode.BinaryMinusNode(startExpression, right, peek.pos)
            }
            is Token.StarToken -> {
                stream.next()
                val right = parseExpression(stream)
                Node.StatementNode.ExpressionNode.BinaryNode.BinaryMultNode(startExpression, right, peek.pos)
            }
            is Token.FSlashToken -> {
                stream.next()
                val right = parseExpression(stream)
                Node.StatementNode.ExpressionNode.BinaryNode.BinaryDivNode(startExpression, right, peek.pos)
            }
            else -> null
        }
    }

    fun parseExpression(stream: TokenStream): Node.StatementNode.ExpressionNode =
        when (val next = stream.next()) {
            is Token.IntegerLiteralToken -> {
                val int = Node.StatementNode.ExpressionNode.IntegerLiteralNode(next.literal, next.pos)
                tryParseBinary(int, stream) ?: int
            }
            is Token.IdentifierToken -> {
                val ref = Node.StatementNode.ExpressionNode.ReferenceNode(next.toIdentifierNode(), next.pos)
                tryParseBinary(ref, stream)
                    ?: tryParseProcCall(next, stream)
                    ?: ref
            }
            else -> throw IllegalStateException("Unidentified expression: $next")
        }

    fun parseProc(token: Token.IdentifierToken, stream: TokenStream): Node.StatementNode.DefProcNode{
        val ident = stream.next()
        if(ident !is Token.IdentifierToken){
            throw RuntimeException("Expect an identifier but instead got $ident")
        }
        var next = stream.next()
        if(next !is Token.LParenToken){
            throw RuntimeException("Expect '(' but instead got $next")
        }
        val params = arrayListOf<Node.StatementNode.ProcParamNode>()
        while(next !is Token.RParenToken){
            next = stream.next()
            if(next !is Token.IdentifierToken){
                throw RuntimeException("Expect an identifier but instead got $ident")
            }
            params.add(Node.StatementNode.ProcParamNode(Node.IdentifierNode(next.lexeme, next.pos), next.pos))
            next = stream.next()
            if(next !is Token.CommaToken){
                if(next !is Token.RParenToken) {
                    throw RuntimeException("Expect a ',' or ')' but instead got $ident")
                }
            }
        }
        next = stream.next()
        if(next !is Token.ColonToken){
            throw RuntimeException("Expect a ':' but instead got $ident")
        }
//        stream.next()
        val body = arrayListOf<Node.StatementNode>()
        while(stream.hasNext() && stream.peek?.pos?.indentLevel!! > token.pos.indentLevel){
            val statement = parseStatement(stream)
            body += statement
        }
        return Node.StatementNode.DefProcNode(Node.IdentifierNode(ident.lexeme, ident.pos), params, body, token.pos)
    }

    fun parseAssignment(stream: TokenStream): Node.StatementNode.ExpressionNode{
        val next = stream.next()
        if(next !is Token.EqualToken){
            throw IllegalArgumentException("Expect '=' but instead got $next")
        }
        return parseExpression(stream)
    }

    fun parseVar(token: Token.IdentifierToken, stream: TokenStream): Node.StatementNode.VarNode{
        val ident = stream.next()
        if(ident !is Token.IdentifierToken){
            throw RuntimeException("Expect an identifier but instead got $ident")
        }
        val expr = parseAssignment(stream)
        return Node.StatementNode.VarNode(Node.IdentifierNode(ident.lexeme, ident.pos), expr, token.pos)
    }

    fun parseLet(token: Token.IdentifierToken, stream: TokenStream): Node.StatementNode.LetNode{
        val ident = stream.next()
        if(ident !is Token.IdentifierToken){
            throw RuntimeException("Expect an identifier but instead got $ident")
        }
        val expr = parseAssignment(stream)
        return Node.StatementNode.LetNode(Node.IdentifierNode(ident.lexeme, ident.pos), expr, token.pos)
    }

    fun parseConst(token: Token.IdentifierToken, stream: TokenStream): Node.StatementNode.ConstNode{
        val ident = stream.next()
        if(ident !is Token.IdentifierToken){
            throw RuntimeException("Expect an identifier but instead got $ident")
        }
        val expr = parseAssignment(stream)
        return Node.StatementNode.ConstNode(Node.IdentifierNode(ident.lexeme, ident.pos), expr, token.pos)
    }

    fun parseReassignment(token: Token.IdentifierToken, stream: TokenStream): Node.StatementNode.ReassignmentNode{
        stream.next()
        val expr = parseExpression(stream)
        return Node.StatementNode.ReassignmentNode(Node.IdentifierNode(token.lexeme, token.pos), expr, token.pos)
    }

    fun tryParseProcCall(token: Token.IdentifierToken, stream: TokenStream): Node.StatementNode.ExpressionNode.ProcCallNode?{
        if(stream.peek !is Token.LParenToken) return null
        val args = arrayListOf<Node.StatementNode.ExpressionNode>()
        stream.next()
        while(stream.peek !is Token.RParenToken && stream.hasNext()){
            args += parseExpression(stream)
            if(stream.peek !is Token.CommaToken){
                if(stream.peek !is Token.RParenToken){
                    throw IllegalStateException("Excepted either ',' or ')' but instead got ${stream.peek}")
                }
                stream.next()
                break
            }
            stream.next()
        }
        return Node.StatementNode.ExpressionNode.ProcCallNode(token.toIdentifierNode(), args, token.pos)
    }

    fun parseStatement(stream: TokenStream): Node.StatementNode{
        return when(val next = stream.next()){
            is Token.IdentifierToken -> {
                when(next.lexeme) {
                    "var" -> parseVar(next, stream)
                    "let" -> parseLet(next, stream)
                    "const" -> parseConst(next, stream)
                    "def" -> parseProc(next, stream)
                    "print" -> parsePrint(next, stream)
                    else -> {
                        when(val peek = stream.peek){
                            is Token.EqualToken -> parseReassignment(next, stream)
                            else -> tryParseProcCall(next, stream) ?: throw RuntimeException("Unrecognized token: $peek")
                        }
                    }
                }
            }
            else -> throw RuntimeException("Unrecognized token: $next")
        }
    }

    fun parseModule(stream: TokenStream): Node.ModuleNode{
        val statements = arrayListOf<Node.StatementNode>()
        while(stream.hasNext()){
            statements.add(parseStatement(stream))
        }
        return Node.ModuleNode(statements)
    }
}