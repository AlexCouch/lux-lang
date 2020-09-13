package parser

import Node
import TokenStream
import arrow.core.*
import errors.SourceAnnotation
import errors.buildSourceAnnotation

class ParseConst: StatementParseRule{
    override fun parse(stream: TokenStream): Either<Node.StatementNode.ConstNode, SourceAnnotation> {
        val next = stream.next()
        if(next is None){
            return buildSourceAnnotation {
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
        }
        val token = (next as Some).t
        val identParser = ParseIdentifier()
        val ident = when(val result = identParser.parse(stream)){
            is Either.Left -> result.a
            is Either.Right -> return result.b.right()
        }
        val typeident = if(stream.peek is Some && (stream.peek as Some).t is Token.ColonToken){
            stream.next()
            val tident = ParseIdentifier()
            when(val result = tident.parse(stream)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
        }else{
            Node.IdentifierNode("dyn", token.startPos, token.endPos)
        }
        val eq = stream.next()
        if(eq is None){
            return buildSourceAnnotation {
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
        }else {
            if ((eq as Some).t !is Token.EqualToken) {
                return buildSourceAnnotation {
                    message = "Expected an '=' but instead got ${eq.t}"
                    errorLine {
                        start = eq.t.startPos
                        end = eq.t.endPos
                    }
                    sourceOrigin {
                        start = eq.t.startPos
                        end = eq.t.endPos
                        source = stream.input
                    }
                }.right()
            }
        }
        val exprParser = ExpressionParser()
        val expr = when(val exprResult = exprParser.parse(stream)){
            is Either.Left -> exprResult.a
            is Either.Right -> return exprResult
        }
        return Node.StatementNode.ConstNode(
            ident,
            expr,
            typeident,
            token.startPos,
            token.endPos
        ).left()
    }
}