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
                    stream.peek
                }else{
                    stream.current
                }
            }else{
                stream.current
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
        while(stream.peek !is None &&
            (stream.peek as Some).t.startPos.indentLevel > (startToken as Some).t.startPos.indentLevel
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
                    stream.peek
                }else{
                    none()
                }
            }else{
                none()
            }
        }else{
            none()
        }
        return Node.StatementNode.ExpressionNode.BlockNode(
            statements,
            if(startToken is None)
                statements[0].startPos
            else
                ((startToken as Some).t).startPos,
            if(end is None)
                statements[0].endPos
            else
                ((startToken as Some).t).endPos
        ).left()
    }

}