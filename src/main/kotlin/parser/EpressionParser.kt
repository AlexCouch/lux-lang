package parser

import Node
import Position
import TokenPos
import TokenStream
import arrow.core.*
import errors.SourceAnnotation
import errors.buildSourceAnnotation

interface ExpressionParseRule: StatementParseRule
class ExpressionParser: ParseRule {
    override fun parse(stream: TokenStream): Either<Node.StatementNode.ExpressionNode, SourceAnnotation> {
        stream.checkpoint()
        val intResult = intParser.parse(stream)
        if(intResult is Either.Left){
            return intResult
        }
        stream.reset()
        val strResult = strParser.parse(stream)
        if(strResult is Either.Left){
            return strResult
        }
        stream.reset()
        val branchingParseResult = branchingParser.parse(stream)
        if(branchingParseResult is Either.Left){
            return branchingParseResult
        }
        stream.reset()
        return buildSourceAnnotation {
            message = "Unrecognized token during parsing: ${stream.current}"
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
    companion object{
        val intParser = IntegerParser()
        val strParser = StringParser()
        val branchingParser = BranchingParser()
    }
}
class IntegerParser: ExpressionParseRule{
    override fun parse(stream: TokenStream): Either<Node.StatementNode.ExpressionNode.IntegerLiteralNode, SourceAnnotation> {
        val int = stream.next()
        if(int is None){
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
            val intconst = (int as Some).t
            if(intconst is Token.IntegerLiteralToken){
                return Node.StatementNode.ExpressionNode.IntegerLiteralNode(intconst.literal, intconst.startPos, intconst.endPos).left()
            }else{
                return buildSourceAnnotation {
                    message = "Expected an integer token but instead got $intconst"
                    errorLine {
                        start = intconst.startPos
                        end = intconst.endPos
                    }
                    sourceOrigin {
                        start = TokenPos(Position(intconst.startPos.pos.line, 0),intconst.startPos.offset - intconst.startPos.pos.col, intconst.startPos.indentLevel)
                        end = intconst.endPos
                        source = stream.input
                    }
                }.right()
            }
        }
    }

}

class StringParser: ExpressionParseRule{
    override fun parse(stream: TokenStream): Either<Node.StatementNode.ExpressionNode.StringLiteralNode, SourceAnnotation> {
        val string = stream.next()
        if(string is None){
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
            val str = (string as Some).t
            if(str is Token.StringLiteralToken){
                return Node.StatementNode.ExpressionNode.StringLiteralNode(str.literal, str.startPos, str.endPos).left()
            }else{
                return buildSourceAnnotation {
                    message = "Expected an integer token but instead got $str"
                    errorLine {
                        start = str.startPos
                        end = str.endPos
                    }
                    sourceOrigin {
                        start = TokenPos(Position(str.startPos.pos.line, 0),str.startPos.offset - str.startPos.pos.col, str.startPos.indentLevel)
                        end = str.endPos
                        source = stream.input
                    }
                }.right()
            }
        }
    }

}