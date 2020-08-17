import arrow.core.*
import arrow.core.Either
import errors.ErrorHandling
import errors.SourceAnnotation
import errors.buildSourceAnnotation
import kotlin.reflect.typeOf

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
    fun visitBinary(binaryNode: Node.StatementNode.ExpressionNode.BinaryNode, parent: P, data: D): R = visitExpression(binaryNode, parent, data)
    fun visitRef(refNode: Node.StatementNode.ExpressionNode.ReferenceNode, parent: P, data: D): R = visitExpression(refNode, parent, data)
    fun visitMutation(mutationNode: Node.StatementNode.ReassignmentNode, parent: P, data: D): R = visitStatement(mutationNode, parent, data)
    fun visitPrint(print: Node.StatementNode.PrintNode, parent: P, data: D): R = visitStatement(print, parent, data)
    fun visitReturn(ret: Node.StatementNode.ReturnNode, parent: P, data: D): R = visitStatement(ret, parent, data)
    fun visitBinaryConditional(conditional: Node.StatementNode.ExpressionNode.BinaryConditionalNode, parent: P, data: D): R = visitExpression(conditional, parent, data)
}

class Parser(val ident: String, val errorHandler: ErrorHandling){

    fun parsePrint(token: Token, stream: TokenStream): Either<Node.StatementNode.PrintNode, SourceAnnotation>{
        return when(val expr = parseExpression(stream)){
            is Either.Left -> Node.StatementNode.PrintNode(expr.a, token.startPos, token.endPos).left()
            is Either.Right -> expr.b.right()
        }
    }

    fun tryParseBinary(startExpression: Node.StatementNode.ExpressionNode, stream: TokenStream): Option<Node.StatementNode.ExpressionNode.BinaryNode> {
        return when (val peek = stream.peek) {
            is Some -> when (val p = peek.t) {
                is Token.PlusToken -> {
                    stream.next()
                    val right = when (val right = parseExpression(stream)) {
                        is Either.Left -> right.a
                        is Either.Right -> return none()
                    }
                    Node.StatementNode.ExpressionNode.BinaryNode.BinaryAddNode(startExpression, right, p.startPos, p.endPos)
                        .some()
                }
                is Token.HyphenToken -> {
                    stream.next()
                    val right = when (val right = parseExpression(stream)) {
                        is Either.Left -> right.a
                        is Either.Right -> return none()
                    }
                    Node.StatementNode.ExpressionNode.BinaryNode.BinaryMinusNode(startExpression, right, p.startPos, p.endPos)
                        .some()
                }
                is Token.StarToken -> {
                    stream.next()
                    val right = when (val right = parseExpression(stream)) {
                        is Either.Left -> right.a
                        is Either.Right -> return none()
                    }
                    Node.StatementNode.ExpressionNode.BinaryNode.BinaryMultNode(startExpression, right, p.startPos, p.endPos)
                        .some()
                }
                is Token.FSlashToken -> {
                    stream.next()
                    val right = when (val right = parseExpression(stream)) {
                        is Either.Left -> right.a
                        is Either.Right -> return none()
                    }
                    Node.StatementNode.ExpressionNode.BinaryNode.BinaryDivNode(startExpression, right, p.startPos, p.endPos)
                        .some()
                }
                else -> none()
            }
            is None -> none()
        }
    }

    fun parseExpression(stream: TokenStream): Either<Node.StatementNode.ExpressionNode, SourceAnnotation> =
        when (val next = stream.next()) {
            is Some -> {
                when(val n = next.t){
                    is Token.IntegerLiteralToken -> {
                        val int = Node.StatementNode.ExpressionNode.IntegerLiteralNode(n.literal, n.startPos, n.endPos)
                        val result = tryParseBinary(int, stream)
                        if(result is Some){
                            result.t.left()
                        }else{
                            int.left()
                        }
                    }
                    is Token.IdentifierToken -> {
                        when(n.toIdentifierNode().str){
                            "if" -> parseBinaryConditional(n, stream)
                            else -> {
                                val ref = Node.StatementNode.ExpressionNode.ReferenceNode(n.toIdentifierNode(), n.startPos, n.endPos)
                                val result = tryParseBinary(ref, stream) or tryParseProcCall(n, stream)
                                if(result is Some){
                                    result.t.left()
                                }else{
                                    ref.left()
                                }
                            }
                        }
                    }
                    else -> buildSourceAnnotation {
                        message = "Unidentified expression: ${next.t}"
                        sourceOrigin {
                            source = stream.input
                            start = next.t.startPos
                            end = next.t.endPos
                        }
                        errorLine {
                            start = next.t.startPos
                            end = next.t.endPos
                        }
                    }.right()
                }
            }
            is None -> buildSourceAnnotation {
                message = "Unexpectedly reached end of token stream. This should only happen in development mode."
                errorLine {
                    start = TokenPos.default
                    end = TokenPos.default
                }
                sourceOrigin {
                    start = TokenPos.default
                    end = TokenPos.default
                    source = stream.input
                }
            }.right()
        }

    fun parseProc(token: Token.IdentifierToken, stream: TokenStream): Either<Node.StatementNode.DefProcNode, SourceAnnotation>{
        val ident = stream.next()
        if(ident is Some){
            if(ident.t !is Token.IdentifierToken){
                return buildSourceAnnotation {
                    message = "Expect an identifier but instead got ${ident.t}"
                }.right()
            }
        }else{
            return buildSourceAnnotation {
                message = "Unexpectedly reached end of token stream. This should only happen in development mode."
                errorLine {
                    start = token.startPos
                    end = token.endPos
                }
                sourceOrigin {
                    start = token.startPos
                    end = token.endPos
                    source = stream.input
                }
            }.right()
        }
        var next = stream.next()
        if(next is Some){
            if(next.t !is Token.LParenToken){
                return buildSourceAnnotation {
                    message = "Expect '(' but instead got ${(next as Some).t}"
                    errorLine {
                        start = token.startPos
                        end = token.endPos
                    }
                    sourceOrigin {
                        start = token.startPos
                        end = token.endPos
                        source = stream.input
                    }
                }.right()
            }
        }else{
            return buildSourceAnnotation {
                message = "Expect '(' but instead got ${(next as Some).t}"
                errorLine {
                    start = token.startPos
                    end = token.endPos
                }
                sourceOrigin {
                    start = token.startPos
                    end = token.endPos
                    source = stream.input
                }
            }.right()
        }
        val params = arrayListOf<Node.StatementNode.ProcParamNode>()
        if(stream.peek is Some && (stream.peek as Some).t !is Token.RParenToken) {
            while (next is Some && next.t !is Token.RParenToken) {
                next = stream.next()
                if (next is Some) {
                    if (next.t !is Token.IdentifierToken) {
                        return buildSourceAnnotation {
                            message = "Expect an identifier but instead got ${(next as Some).t}"
                            errorLine {
                                start = token.startPos
                                end = token.endPos
                            }
                            sourceOrigin {
                                start = token.startPos
                                end = token.endPos
                                source = stream.input
                            }
                        }.right()
                    }
                }
                val paramident = next
                val type = if (stream.peek is Some) {
                    val peek = stream.peek as Some
                    if (peek.t is Token.ColonToken) {
                        stream.next()
                        val tyident = stream.next()
                        if (tyident is Some) {
                            if (tyident.t !is Token.IdentifierToken) {
                                return buildSourceAnnotation {
                                    message =
                                        "Unexpectedly reached end of token stream. This should only happen in development mode."
                                    errorLine {
                                        start = token.startPos
                                        end = token.endPos
                                    }
                                    sourceOrigin {
                                        start = token.startPos
                                        end = token.endPos
                                        source = stream.input
                                    }
                                }.right()
                            } else {
                                Node.IdentifierNode(
                                    (tyident.t as Token.IdentifierToken).lexeme,
                                    tyident.t.startPos,
                                    tyident.t.endPos
                                )
                            }
                        } else {
                            return buildSourceAnnotation {
                                message =
                                    "Unexpectedly reached end of token stream. This should only happen in development mode."
                                errorLine {
                                    start = token.startPos
                                    end = token.endPos
                                }
                                sourceOrigin {
                                    start = token.startPos
                                    end = token.endPos
                                    source = stream.input
                                }
                            }.right()
                        }
                    } else {
                        Node.IdentifierNode("dyn", TokenPos.default, TokenPos.default)
                    }
                } else {
                    return buildSourceAnnotation {
                        message =
                            "Unexpectedly reached end of token stream. This should only happen in development mode."
                        errorLine {
                            start = token.startPos
                            end = token.endPos
                        }
                        sourceOrigin {
                            start = token.startPos
                            end = token.endPos
                            source = stream.input
                        }
                    }.right()
                }
                when (paramident) {
                    is Some -> {
                        if (paramident.t is Token.IdentifierToken) {
                            val ident = paramident.t as Token.IdentifierToken
                            params.add(
                                Node.StatementNode.ProcParamNode(
                                    Node.IdentifierNode(
                                        ident.lexeme,
                                        ident.startPos,
                                        ident.endPos
                                    ),
                                    type,
                                    ident.startPos,
                                    ident.endPos
                                )
                            )
                        }
                    }
                    is None -> {
                        return buildSourceAnnotation {
                            message =
                                "Unexpectedly reached end of token stream. This should only happen during development mode."
                            errorLine {
                                start = token.startPos
                                end = token.endPos
                            }
                            sourceOrigin {
                                start = token.startPos
                                end = token.endPos
                                source = stream.input
                            }
                        }.right()
                    }
                }
                next = stream.next()
                when (next) {
                    is Some -> {
                        if (next.t !is Token.CommaToken) {
                            if (next.t !is Token.RParenToken) {
                                val t = next.t
                                return buildSourceAnnotation {
                                    message = "Expect a ',' or ')' but instead got $t"
                                    errorLine {
                                        start = t.startPos
                                        end = t.endPos
                                    }
                                    sourceOrigin {
                                        start = t.startPos
                                        end = t.endPos
                                        source = stream.input
                                    }
                                }.right()
                            }
                        }
                    }
                    is None -> {
                        return buildSourceAnnotation {
                            message =
                                "Unexpectedly reached end of token stream. This should only happen during development mode."
                            errorLine {
                                start = token.startPos
                                end = token.endPos
                            }
                            sourceOrigin {
                                start = token.startPos
                                end = token.endPos
                                source = stream.input
                            }
                        }.right()
                    }
                }
            }
        }
        next = stream.next()
        val returnType = if(next is Some){
            if(next.t is Token.HyphenToken){
                next = stream.next()
                if(next is Some){
                    if(next.t !is Token.RAngleToken){
                        val t = next.t
                        return buildSourceAnnotation {
                            message = "Expect a '>' but instead got $t"
                            errorLine {
                                start = t.startPos
                                end = t.endPos
                            }
                            sourceOrigin {
                                start = t.startPos
                                end = t.endPos
                                source = stream.input
                            }
                        }.right()
                    }
                }
                next = stream.next()
                if(next is Some){
                    if(next.t !is Token.IdentifierToken){
                        val t = next.t
                        return buildSourceAnnotation {
                            message = "Expect an identifier but instead got $t"
                            errorLine {
                                start = t.startPos
                                end = t.endPos
                            }
                            sourceOrigin {
                                start = t.startPos
                                end = t.endPos
                                source = stream.input
                            }
                        }.right()
                    }else{
                        val node = Node.IdentifierNode((next.t as Token.IdentifierToken).lexeme, next.t.startPos, next.t.endPos)
                        next = stream.next()
                        node
                    }
                }else{
                    next = stream.next()
                    Node.IdentifierNode("dyn", TokenPos.default, TokenPos.default)
                }
            }else{
                next = stream.next()
                Node.IdentifierNode("dyn", TokenPos.default, TokenPos.default)
            }
        }else{
            return buildSourceAnnotation {
                message = "Unexpectedly reached end of token stream. This should only happen during development mode."
                errorLine {
                    start = token.startPos
                    end = token.endPos
                }
                sourceOrigin {
                    start = token.startPos
                    end = token.endPos
                    source = stream.input
                }
            }.right()
        }
//        next = stream.next()
        if(next is Some){
            if(next.t !is Token.ColonToken){
                val t = next.t
                return buildSourceAnnotation {
                    message = "Expect a ':' but instead got $t"
                    errorLine {
                        start = t.startPos
                        end = t.endPos
                    }
                    sourceOrigin {
                        start = t.startPos
                        end = t.endPos
                        source = stream.input
                    }
                }.right()
            }
        }
//        stream.next()
        val body = arrayListOf<Node.StatementNode>()
        if(stream.peek is Some){
            stream.next()
            while(stream.hasNext() && stream.peek is Some<Token> && (stream.peek as Some<Token>).t.startPos.indentLevel > token.startPos.indentLevel){
                val statement = parseStatement(stream)
                if(statement is Either.Left){
                    body += statement.a
                }else{
                    return (statement as Either.Right).b.right()
                }
                if((stream.peek as Some<Token>).t.startPos.indentLevel > token.startPos.indentLevel){
                    stream.next()
                }
            }
        }
        val t = ident.t as Token.IdentifierToken
        return Node.StatementNode.DefProcNode(Node.IdentifierNode(t.lexeme, t.startPos, t.endPos), params, body, returnType, token.startPos, token.endPos).left()
    }

    fun parseAssignment(stream: TokenStream): Either<Node.StatementNode.ExpressionNode, SourceAnnotation>{
        val next = stream.next()
        when(next){
            is Some -> {
                if(next.t !is Token.EqualToken){
                    val t = next.t
                    return buildSourceAnnotation {
                        message = "Expect '=' but instead got $t"
                        errorLine {
                            start = t.startPos
                            end = t.endPos
                        }
                        sourceOrigin {
                            start = t.startPos
                            end = t.endPos
                            source = stream.input
                        }
                    }.right()
                }
            }
            is None -> buildSourceAnnotation {
                message = "Unexpectedly reached end of token stream. This should only happen during development mode."
            }
        }
        return parseExpression(stream)
    }

    fun parseVar(token: Token.IdentifierToken, stream: TokenStream): Either<Node.StatementNode.VarNode, SourceAnnotation> {
        val ident = stream.next()
        when(ident){
            is Some -> {
                if(ident.t !is Token.IdentifierToken){
                    return buildSourceAnnotation {
                        message = "Expected an identifier but instead got ${ident.t}"
                        errorLine {
                            start = ident.t.startPos
                            end = ident.t.endPos
                        }
                        sourceOrigin {
                            start = ident.t.startPos
                            end = ident.t.endPos
                            source = stream.input
                        }
                    }.right()
                }
            }
            is None -> return buildSourceAnnotation {
                message = "Unexpectedly reached end of token stream. This should only happen in development mode."
                errorLine {
                    start = token.startPos
                    end = token.endPos
                }
                sourceOrigin {
                    start = token.startPos
                    end = token.endPos
                    source = stream.input
                }
            }.right()
        }
        val typeAnnot = if(stream.peek is Some){
            val peek = stream.peek as Some<Token>
            if(peek.t is Token.ColonToken){
                val typeident = stream.next()
                if(typeident is Some){
                    val t = typeident.t
                    if(t !is Token.IdentifierToken){
                        return buildSourceAnnotation {
                            message = "Expected an identifier but instead got ${typeident.t}"
                            errorLine {
                                start = t.startPos
                                end = t.endPos
                            }
                            sourceOrigin {
                                start = t.startPos
                                end = t.endPos
                                source = stream.input
                            }
                        }.right()
                    }
                    Node.IdentifierNode(t.lexeme, t.startPos, t.startPos)
                }else{
                    return buildSourceAnnotation {
                        message = "Expected an identifier but instead got ${ident.t}"
                        errorLine {
                            start = ident.t.startPos
                            end = ident.t.endPos
                        }
                        sourceOrigin {
                            start = ident.t.startPos
                            end = ident.t.endPos
                            source = stream.input
                        }
                    }.right()
                }
            }else{
                Node.IdentifierNode("dyn", TokenPos.default, TokenPos.default)
            }
        }else{
            return buildSourceAnnotation {
                message = "Unexpectedly reached end of token stream. This should only happen in development mode."
                errorLine {
                    start = token.startPos
                    end = token.endPos
                }
                sourceOrigin {
                    start = token.startPos
                    end = token.endPos
                    source = stream.input
                }
            }.right()
        }
        val expr = parseAssignment(stream)
        return if(expr is Either.Left){
            val t = expr.a
            Node.StatementNode.VarNode(Node.IdentifierNode((ident.t as Token.IdentifierToken).lexeme, token.startPos, token.endPos), t, typeAnnot, token.startPos, token.endPos).left()
        }else{
            (expr as Either.Right).b.right()
        }
    }

    fun parseLet(token: Token.IdentifierToken, stream: TokenStream): Either<Node.StatementNode.LetNode, SourceAnnotation>{
        val ident = stream.next()
        when(ident){
            is Some -> {
                if(ident.t !is Token.IdentifierToken){
                    return buildSourceAnnotation {
                        message = "Expected an identifier but instead got ${ident.t}"
                        errorLine {
                            start = ident.t.startPos
                            end = ident.t.endPos
                        }
                        sourceOrigin {
                            start = ident.t.startPos
                            end = ident.t.endPos
                            source = stream.input
                        }
                    }.right()
                }
            }
            is None -> return buildSourceAnnotation {
                message = "Unexpectedly reached end of token stream. This should only happen in development mode."
                errorLine {
                    start = token.startPos
                    end = token.endPos
                }
                sourceOrigin {
                    start = token.startPos
                    end = token.endPos
                    source = stream.input
                }
            }.right()
        }
        return when(stream.peek){
            is Some -> {
                val typeAnnot = if(stream.peek is Some){
                    val peek = stream.peek as Some<Token>
                    if(peek.t is Token.ColonToken){
                        val typeident = stream.next()
                        if(typeident is Some){
                            val t = typeident.t
                            if(typeident.t !is Token.IdentifierToken){
                                return buildSourceAnnotation {
                                    message = "Expected an identifier but instead got $t"
                                    errorLine {
                                        start = t.startPos
                                        end = t.endPos
                                    }
                                    sourceOrigin {
                                        start = t.startPos
                                        end = t.endPos
                                        source = stream.input
                                    }
                                }.right()
                            }else{
                                t as Token.IdentifierToken
                                Node.IdentifierNode(t.lexeme, t.startPos, t.endPos)
                            }
                        }else{
                            return buildSourceAnnotation {
                                message = "Unexpectedly reached end of token stream. This should only happen in development mode."
                                errorLine {
                                    start = token.startPos
                                    end = token.endPos
                                }
                                sourceOrigin {
                                    start = token.startPos
                                    end = token.endPos
                                    source = stream.input
                                }
                            }.right()
                        }
                    }else{
                        Node.IdentifierNode("dyn", TokenPos.default, TokenPos.default)
                    }
                }else{
                    return buildSourceAnnotation {
                        message = "Unexpectedly reached end of token stream. This should only happen in development mode."
                        errorLine {
                            start = token.startPos
                            end = token.endPos
                        }
                        sourceOrigin {
                            start = token.startPos
                            end = token.endPos
                            source = stream.input
                        }
                    }.right()
                }
                val expr = parseAssignment(stream)
                if(expr is Either.Left){
                    Node.StatementNode.LetNode(Node.IdentifierNode((ident.t as Token.IdentifierToken).lexeme, ident.t.startPos, ident.t.endPos), expr.a, typeAnnot, token.startPos, token.endPos).left()
                }else{
                    (expr as Either.Right).b.right()
                }
            }
            is None -> buildSourceAnnotation {
                message = "Unexpectedly reached end of token stream. This should only happen in development mode."
                errorLine {
                    start = token.startPos
                    end = token.endPos
                }
                sourceOrigin {
                    start = token.startPos
                    end = token.endPos
                    source = stream.input
                }
            }.right()
        }
    }

    fun parseConst(token: Token.IdentifierToken, stream: TokenStream): Either<Node.StatementNode.ConstNode, SourceAnnotation>{
        val ident = stream.next()
        when(ident){
            is Some -> {
                if(ident.t !is Token.IdentifierToken){
                    return buildSourceAnnotation {
                        message = "Expected an identifier but instead got ${ident.t}"
                        errorLine {
                            start = ident.t.startPos
                            end = ident.t.endPos
                        }
                        sourceOrigin {
                            start = ident.t.startPos
                            end = ident.t.endPos
                            source = stream.input
                        }
                    }.right()
                }
            }
            is None -> return buildSourceAnnotation {
                message = "Unexpectedly reached end of token stream. This should only happen in development mode."
                errorLine {
                    start = token.startPos
                    end = token.endPos
                }
                sourceOrigin {
                    start = token.startPos
                    end = token.endPos
                    source = stream.input
                }
            }.right()
        }
        val typeAnnot = if(stream.peek is Some){
            val peek = stream.peek as Some
            if(peek.t is Token.ColonToken){
                val typeident = stream.next()
                if(typeident is Some){
                    if(typeident.t !is Token.IdentifierToken){
                        throw IllegalArgumentException("Expected an identifier but instead got $typeident")
                    }
                }else{
                    return buildSourceAnnotation {
                        message = "Unexpectedly reached end of token stream. This should only happen in development mode."
                        errorLine {
                            start = token.startPos
                            end = token.endPos
                        }
                        sourceOrigin {
                            start = token.startPos
                            end = token.endPos
                            source = stream.input
                        }
                    }.right()
                }
                Node.IdentifierNode((typeident.t as Token.IdentifierToken).lexeme, typeident.t.startPos, typeident.t.endPos)
            }else{
                Node.IdentifierNode("dyn", TokenPos.default, TokenPos.default)
            }
        }else{
            return buildSourceAnnotation {
                message = "Unexpectedly reached end of token stream. This should only happen in development mode."
                errorLine {
                    start = token.startPos
                    end = token.endPos
                }
                sourceOrigin {
                    start = token.startPos
                    end = token.endPos
                    source = stream.input
                }
            }.right()
        }
        val expr = parseAssignment(stream)
        return if(expr.isLeft()){
            Node.StatementNode.ConstNode(
                Node.IdentifierNode(
                    (ident.t as Token.IdentifierToken).lexeme, ident.t.startPos, ident.t.endPos
                ),
                (expr as Either.Left).a,
                typeAnnot,
                token.startPos,
                token.endPos
            ).left()
        }else{
            (expr as Either.Right).b.right()
        }
    }

    fun parseReassignment(token: Token.IdentifierToken, stream: TokenStream): Either<Node.StatementNode.ReassignmentNode, SourceAnnotation>{
        stream.next()
        val expr = parseExpression(stream)
        return when(expr){
            is Either.Left -> Node.StatementNode.ReassignmentNode(Node.IdentifierNode(token.lexeme, token.startPos, token.endPos), expr.a, token.startPos, token.endPos).left()
            is Either.Right -> return buildSourceAnnotation {
                message = "Unexpectedly reached end of token stream. This should only happen in development mode."
                errorLine {
                    start = token.startPos
                    end = token.endPos
                }
                sourceOrigin {
                    start = token.startPos
                    end = token.endPos
                    source = stream.input
                }
            }.right()
        }
    }

    fun tryParseProcCall(token: Token.IdentifierToken, stream: TokenStream): Option<Node.StatementNode.ExpressionNode.ProcCallNode>{
        if(stream.peek is Some){
            val peek = stream.peek as Some
            if(peek.t !is Token.LParenToken) return none()
        }else{
            return none()
        }
        val args = arrayListOf<Node.StatementNode.ExpressionNode>()
        stream.next()
        while(stream.peek is Some<Token> && stream.hasNext()){
            val peek = stream.peek as Some
            if(peek.t is Token.RParenToken) break
            args += when(val expr = parseExpression(stream)){
                is Either.Left -> expr.a
                is Either.Right -> return none()
            }
            if(stream.peek is Some){
                val peek = stream.peek as Some<Token>
                if(peek.t !is Token.CommaToken){
                    if(peek.t !is Token.RParenToken){
                        return none()
                    }
                    stream.next()
                    break
                }
            }
            stream.next()
        }
        return Node.StatementNode.ExpressionNode.ProcCallNode(token.toIdentifierNode(), args, token.startPos, token.endPos).some()
    }

    fun parseReturn(token: Token, stream: TokenStream): Either<Node.StatementNode.ReturnNode, SourceAnnotation>{
        val expr = parseExpression(stream)
        return when(expr){
            is Either.Left -> Node.StatementNode.ReturnNode(expr.a, token.startPos, token.endPos).left()
            is Either.Right -> expr
        }
    }

    fun parseBlock(token: Token, stream: TokenStream): Either<Node.StatementNode.ExpressionNode.BlockNode, SourceAnnotation>{
        val stmts = arrayListOf<Node.StatementNode>()
        while(
            stream.hasNext() &&
            (stream.peek is Some  && (stream.peek as Some).t.startPos.indentLevel > token.startPos.indentLevel)){
            stmts.add(when(val stmt = parseStatement(stream)){
                is Either.Left -> stmt.a
                is Either.Right -> return stmt
            })
            stream.next()
        }
        return Node.StatementNode.ExpressionNode.BlockNode(stmts, token.startPos, (stream.current as Some).t.endPos).left()
    }

    fun parseBinaryConditional(
        token: Token,
        stream: TokenStream
    ): Either<
            Node.StatementNode.ExpressionNode.BinaryConditionalNode,
            SourceAnnotation>{
        val condition = when(val condition = parseExpression(stream)){
            is Either.Left -> condition.a
            is Either.Right -> return condition.b.right()
        }
        when(val colon = stream.next()){
            is Some -> {
                when(colon.t){
                    is Token.ColonToken -> {}
                    else ->
                        return buildSourceAnnotation {
                            message = "Expected ':' but instead found ${colon.t}"
                            errorLine {
                                start = colon.t.startPos
                                end = colon.t.endPos
                            }
                            sourceOrigin {
                                start = token.startPos
                                end = colon.t.endPos
                            }
                        }.right()
                }

            }
            else -> return buildSourceAnnotation {
                message = "Expected ':' but instead found EOF"
                errorLine {
                    start = condition.endPos
                    end = condition.endPos
                }
                sourceOrigin {
                    start = token.startPos
                    end = condition.endPos
                }
            }.right()
        }

        val block = when(val block = parseBlock(token, stream)){
            is Either.Left -> block.a
            is Either.Right -> return block
        }
        val current = stream.current
        if(current is Some && current.t is Token.IdentifierToken){
            if((current.t as Token.IdentifierToken).lexeme == "else"){
                when(val colon = stream.next()){
                    is Some -> {
                        when(colon.t){
                            is Token.ColonToken -> {}
                            else ->
                                return buildSourceAnnotation {
                                    message = "Expected ':' but instead found ${colon.t}"
                                    errorLine {
                                        start = colon.t.startPos
                                        end = colon.t.endPos
                                    }
                                    sourceOrigin {
                                        start = token.startPos
                                        end = colon.t.endPos
                                    }
                                }.right()
                        }

                    }
                    else -> return buildSourceAnnotation {
                        message = "Expected ':' but instead found EOF"
                        errorLine {
                            start = condition.endPos
                            end = condition.endPos
                        }
                        sourceOrigin {
                            start = token.startPos
                            end = condition.endPos
                        }
                    }.right()
                }
                return when(val elseBlock = parseBlock(current.t, stream)){
                    is Either.Left ->
                        Node.StatementNode.ExpressionNode.BinaryConditionalNode(
                            condition,
                            block,
                            elseBlock.a.some(),
                            token.startPos,
                            elseBlock.a.endPos
                        ).left()
                    is Either.Right ->
                        elseBlock
                }
            }
        }
        return Node.StatementNode.ExpressionNode.BinaryConditionalNode(condition, block, none(), token.startPos, block.endPos).left()
    }

    fun parseStatement(stream: TokenStream): Either<Node.StatementNode, SourceAnnotation>{
        return when(val peekNext = stream.current){
            is Some -> {
                when(val n = peekNext.t){
                    is Token.IdentifierToken -> {
                        when(n.lexeme) {
                            "var" -> parseVar(n, stream)
                            "let" -> parseLet(n, stream)
                            "const" -> parseConst(n, stream)
                            "def" -> parseProc(n, stream)
                            "print" -> parsePrint(n, stream)
                            "return" -> parseReturn(n, stream)
                            "if" -> parseBinaryConditional(n, stream)
//                            "when" -> parseWhen(n, stream)
                            else -> {
                                when(val peek = stream.peek){
                                    is Some -> {
                                        when(peek.t){
                                            is Token.EqualToken -> parseReassignment(n, stream)
                                            else -> {
                                                val expr = parseExpression(stream)
                                                if(expr.isLeft()){
                                                    return expr
                                                }

                                                return buildSourceAnnotation {
                                                    message = "No recognized statement."
                                                    errorLine {
                                                        start = peek.t.startPos
                                                        end = peek.t.endPos
                                                    }
                                                    sourceOrigin {
                                                        start = peek.t.startPos
                                                        end = peek.t.endPos
                                                        source = stream.input
                                                    }
                                                }.right()
                                            }
                                        }
                                    }
                                    is None ->{
                                        buildSourceAnnotation {
                                            message = "Unexpected end of token stream. This should only happen during development mode."
                                            errorLine {
                                                start = n.startPos
                                                end = n.endPos
                                            }
                                            sourceOrigin {
                                                start = n.startPos
                                                end = n.endPos
                                                source = stream.input
                                            }
                                        }.right()
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        val expr = parseExpression(stream)
                        if(expr.isLeft()){
                            return expr
                        }

                        return buildSourceAnnotation {
                            message = "No recognized statement."
                            errorLine {
                                start = n.startPos
                                end = n.endPos
                            }
                            sourceOrigin {
                                start = n.startPos
                                end = n.endPos
                                source = stream.input
                            }
                        }.right()
                    }
                }
            }
            is None -> buildSourceAnnotation {
                message = "Unexpected end of token stream. This should only happen during development mode."
                errorLine {
                    start = TokenPos.default
                    end = TokenPos.default
                }
                sourceOrigin {
                    start = TokenPos.default
                    end = TokenPos.default
                    source = stream.input
                }
            }.right()
        } as Either<Node.StatementNode, SourceAnnotation>
    }

    fun parseModule(stream: TokenStream): Option<Node.ModuleNode>{
        val statements = arrayListOf<Node.StatementNode>()
        stream.next()
        while(stream.hasNext()){
            when(val statement = parseStatement(stream)){
                is Either.Left -> statements.add(statement.a)
                is Either.Right -> {
                    errorHandler.error {
                        message = "An error occurred while parsing"
                        addAnnotation(statement.b)
                    }
                    return none()
                }
            }
            stream.next()
        }
        return Node.ModuleNode(Node.IdentifierNode(this.ident, TokenPos.default, TokenPos.default), statements).some()
    }
}