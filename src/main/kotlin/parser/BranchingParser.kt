package parser

import Node
import Position
import TokenPos
import TokenStream
import arrow.core.*
import errors.SourceAnnotation
import errors.buildSourceAnnotation

interface BranchingParseRule: ParseRule
class BranchingParser: ParseRule{
    override fun parse(stream: TokenStream): Either<Node.StatementNode.ExpressionNode.ConditionalBranchingNode, SourceAnnotation> {
        val current = if(stream.current is None){
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
        }else{
            stream.current as Some
        }
        stream.checkpoint()
        val binaryParser = binaryBranchingParser.parse(stream)
        if(binaryParser is Either.Left){
            return binaryParser
        }
        stream.reset()
        return buildSourceAnnotation {
            message = "Expected a conditional expression start token such as 'if' or 'when' but instead got ${current.t}"
            errorLine {
                start = current.t.startPos
                end = current.t.endPos
            }
            sourceOrigin {
                start = TokenPos(Position(current.t.startPos.pos.line, 0),current.t.startPos.offset - current.t.startPos.pos.col, current.t.startPos.indentLevel)
                end = current.t.endPos
                source = stream.input
            }
        }.right()
    }

    companion object{
        val binaryBranchingParser = BinaryBranchingParseRule()
    }

}

class BinaryBranchingParseRule: BranchingParseRule{
    override fun parse(stream: TokenStream): Either<Node.StatementNode.ExpressionNode.ConditionalBranchingNode, SourceAnnotation> {
        val ifToken = when(val next = stream.next()){
            is Some -> if(next.t is Token.IdentifierToken){
                if((next.t as Token.IdentifierToken).lexeme == "if"){
                    next.t
                }else{
                    return buildSourceAnnotation {
                        message = "Expected 'if' token but instead got ${next.t}"
                        errorLine {
                            start = next.t.startPos
                            end = next.t.endPos
                        }
                        sourceOrigin {
                            start = TokenPos(Position(next.t.startPos.pos.line, 0),next.t.startPos.offset - next.t.startPos.pos.col, next.t.startPos.indentLevel)
                            end = next.t.endPos
                            source = stream.input
                        }
                    }.right()
                }
            }else{
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
        val expr = when(val result = exprParseRule.parse(stream)){
            is Either.Left -> result.a
            is Either.Right -> return result
        }
        val consequence = when(val result = blockParser.parse(stream)){
            is Either.Left -> result.a
            is Either.Right -> return result
        }
        val elseToken = when(val next = stream.next()){
            is Some -> if(next.t is Token.IdentifierToken){
                if((next.t as Token.IdentifierToken).lexeme == "else"){
                    next.t
                }else{
                    return Node.StatementNode.ExpressionNode.ConditionalBranchingNode.ConditionalBranchNode(expr, consequence, ifToken.startPos, consequence.endPos).left()
                }
            }else{
                return Node.StatementNode.ExpressionNode.ConditionalBranchingNode.ConditionalBranchNode(expr, consequence, ifToken.startPos, consequence.endPos).left()
            }
            is None -> return buildSourceAnnotation {
                message = "Unexpected end of token stream while parsing binary. This should only happen during development mode."
                errorLine {
                    start = expr.startPos
                    end = expr.endPos
                }
                sourceOrigin {
                    start = TokenPos(Position(expr.startPos.pos.line, 0),expr.startPos.offset - expr.startPos.pos.col, expr.startPos.indentLevel)
                    end = expr.endPos
                    source = stream.input
                }
            }.right()
        }
        val otherwise = when(val result = blockParser.parse(stream)){
            is Either.Left -> result.a
            is Either.Right -> return result
        }
        return Node.StatementNode.ExpressionNode.ConditionalBranchingNode.BinaryConditionalNode(expr, consequence, otherwise.some(), ifToken.startPos, consequence.endPos).left()
    }

    companion object{
        val exprParseRule = ExpressionParser()
        val blockParser = BlockParser()
    }

}