package parser

import Node
import TokenStream
import arrow.core.*
import errors.SourceAnnotation
import errors.buildSourceAnnotation

class PrintParser: StatementParseRule{
    override fun parse(stream: TokenStream): Either<Node.StatementNode.PrintNode, SourceAnnotation> {
        val print = when(val next = stream.next()){
            is Some -> if(next.t is Token.IdentifierToken){
                if((next.t as Token.IdentifierToken).lexeme == "print"){
                    next.t
                }else{
                    return buildSourceAnnotation {
                        message = "Expected a 'print' token but instead got ${next.t}"
                        errorLine {
                            start = next.t.startPos
                            end = next.t.endPos
                        }
                        sourceOrigin {
                            start = next.t.startPos
                            end = next.t.endPos
                            source = stream.input
                        }
                    }.right()
                }
            }else{
                return buildSourceAnnotation {
                    message = "Expected a 'print' token but instead got ${next.t}"
                    errorLine {
                        start = next.t.startPos
                        end = next.t.endPos
                    }
                    sourceOrigin {
                        start = next.t.startPos
                        end = next.t.endPos
                        source = stream.input
                    }
                }.right()
            }
            is None -> return buildSourceAnnotation {
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
        val exprResult = exprParser.parse(stream)
        if(exprResult is Either.Right){
            return exprResult
        }
        val expr = (exprResult as Either.Left).a
        return Node.StatementNode.PrintNode(expr, print.startPos, expr.endPos).left()
    }

    companion object{
        val exprParser = ExpressionParser()
    }

}