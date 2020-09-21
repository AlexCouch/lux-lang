package parser

import Node
import Token
import TokenStream
import arrow.core.*
import errors.SourceAnnotation
import errors.buildSourceAnnotation

class BlockParser: ExpressionParseRule{
    override fun parse(stream: TokenStream): Either<Node.StatementNode.ExpressionNode.BlockNode, SourceAnnotation> {
        val statements = arrayListOf<Node.StatementNode>()
        val startToken = if(stream.peek is Some){
            val peek = (stream.peek as Some).t
            if(peek is Token.IdentifierToken){
                if(peek.lexeme == "do"){
                    (stream.peek as Some).t
                }else{
                    (stream.current as Some).t
                }
            }else{
                (stream.current as Some).t
            }
        }else{
            return buildSourceAnnotation {
                message = "Unexpected end of token stream while parsing block. This should only happen during development mode."
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
        if(stream.peek !is None &&
            (stream.peek as Some).t.startPos.indentLevel <= startToken.startPos.indentLevel){
            return buildSourceAnnotation {
                message = "Indentation of statements within block must be at least 1 indentation level ahead of the start of the block"
                errorLine {
                    start = (stream.peek as Some).t.startPos
                    end = (stream.peek as Some).t.endPos
                }
                sourceOrigin {
                    start = (stream.peek as Some).t.startPos
                    end = (stream.peek as Some).t.endPos
                    source = stream.input
                }
            }.right()
        }
        while(stream.peek !is None &&
            (stream.peek as Some).t.startPos.indentLevel > startToken.startPos.indentLevel
        ){
            when(val statement = ModuleParser.statementParser.parse(stream)) {
                is Either.Left -> statements.add(statement.a)
                is Either.Right -> return statement
            }
        }
        val end = if(stream.peek is Some){
            val peek = (stream.peek as Some).t
            if(peek is Token.IdentifierToken){
                if(peek.lexeme == "end"){
                    (stream.peek as Some).t
                }else{
                    (stream.current as Some).t
                }
            }else{
                (stream.current as Some).t
            }
        }else{
            (stream.current as Some).t
        }
        return Node.StatementNode.ExpressionNode.BlockNode(
            statements,
            startToken.startPos,
            end.endPos
        ).left()
    }

}