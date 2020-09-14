package parser

import Node
import Position
import Token
import TokenPos
import TokenStream
import arrow.core.*
import com.sun.org.apache.xpath.internal.ExpressionNode
import errors.SourceAnnotation
import errors.buildSourceAnnotation
import jdk.nashorn.internal.ir.BinaryNode

interface ExpressionParseRule: StatementParseRule
class ExpressionParser: ParseRule {
    override fun parse(stream: TokenStream): Either<Node.StatementNode.ExpressionNode, SourceAnnotation> {
        stream.checkpoint()
        val intResult = intParser.parse(stream)
        if(intResult is Either.Left){
            if(checkForBinary(stream)){
                val binaryParseResult = binaryParser.parse(intResult.a, stream)
                if(binaryParseResult is Either.Left){
                    return binaryParseResult
                }
            }
            return intResult
        }
        stream.reset()
        val strResult = strParser.parse(stream)
        if(strResult is Either.Left){
            if(checkForBinary(stream)){
                val binaryParseResult = binaryParser.parse(strResult.a, stream)
                if(binaryParseResult is Either.Left){
                    return binaryParseResult
                }
            }
            return strResult
        }
        stream.reset()
        val branchingParseResult = branchingParser.parse(stream)
        if(branchingParseResult is Either.Left){
            if(checkForBinary(stream)){
                val binaryParseResult = binaryParser.parse(branchingParseResult.a, stream)
                if(binaryParseResult is Either.Left){
                    return binaryParseResult
                }
            }
            return branchingParseResult
        }
        stream.reset()
        val procCallResult = procCallParser.parse(stream)
        if(procCallResult is Either.Left){
            if(checkForBinary(stream)){
                val binaryParseResult = binaryParser.parse(procCallResult.a, stream)
                if(binaryParseResult is Either.Left){
                    return binaryParseResult
                }
            }
            return procCallResult
        }
        stream.reset()
        val refParseResult = refParser.parse(stream)
        if(refParseResult is Either.Left){
            if(checkForBinary(stream)){
                val binaryParseResult = binaryParser.parse(refParseResult.a, stream)
                if(binaryParseResult is Either.Left){
                    return binaryParseResult
                }
            }
            return refParseResult
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
    fun checkForBinary(stream: TokenStream) =
        stream.peek is Some
        && ((stream.peek as Some).t is Token.PlusToken
        || (stream.peek as Some).t is Token.HyphenToken
        || (stream.peek as Some).t is Token.StarToken
        || (stream.peek as Some).t is Token.FSlashToken)

    companion object{
        val intParser = IntegerParser()
        val strParser = StringParser()
        val branchingParser = BranchingParser()
        val binaryParser = BinaryParser()
        val refParser = ReferenceParser()
        val procCallParser = ProcCallParser()
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

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class BinaryParser: RecursiveParseRule{
    override fun parse(left: Node, stream: TokenStream): Either<Node.StatementNode.ExpressionNode.BinaryNode, SourceAnnotation> {
        if(left !is Node.StatementNode.ExpressionNode){
            return buildSourceAnnotation {
                message = "Expected an expression as left operand but instead got $left"
                errorLine {
                    start = left.startPos
                    end = left.endPos
                }
                sourceOrigin {
                    start = TokenPos(Position(left.startPos.pos.line, 0),left.startPos.offset - left.startPos.pos.col, left.startPos.indentLevel)
                    end = left.endPos
                    source = stream.input
                }
            }.right()
        }
        val operator = when(val operator = stream.next()){
            is Some -> operator.t
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
        val right = when(val right = exprParser.parse(stream)){
            is Either.Left -> right.a
            is Either.Right -> return right
        }
        return when(operator){
            is Token.PlusToken ->  Node.StatementNode.ExpressionNode.BinaryNode.BinaryAddNode(left, right, left.startPos, right.endPos).left()
            is Token.HyphenToken ->  Node.StatementNode.ExpressionNode.BinaryNode.BinaryMinusNode(left, right, left.startPos, right.endPos).left()
            is Token.StarToken ->  Node.StatementNode.ExpressionNode.BinaryNode.BinaryMultNode(left, right, left.startPos, right.endPos).left()
            is Token.FSlashToken ->  Node.StatementNode.ExpressionNode.BinaryNode.BinaryDivNode(left, right, left.startPos, right.endPos).left()
            else -> buildSourceAnnotation {
                message = "Unrecognized binary operator: $operator"
                errorLine {
                    start = operator.startPos
                    end = operator.endPos
                }
                sourceOrigin {
                    start = TokenPos(Position(operator.startPos.pos.line, 0),operator.startPos.offset - operator.startPos.pos.col, operator.startPos.indentLevel)
                    end = operator.endPos
                    source = stream.input
                }
            }.right()
        }
    }

    companion object{
        val exprParser = ExpressionParser()
    }

}

class ReferenceParser: ExpressionParseRule{
    override fun parse(stream: TokenStream): Either<Node.StatementNode.ExpressionNode.ReferenceNode, SourceAnnotation> {
        val identifier = when(val ident = identParser.parse(stream)){
            is Either.Left -> ident.a
            is Either.Right -> return ident
        }

        return Node.StatementNode.ExpressionNode.ReferenceNode(identifier, identifier.startPos, identifier.endPos).left()
    }

    companion object{
        val identParser = ParseIdentifier()
    }

}

class ProcCallParser: ExpressionParseRule{
    override fun parse(stream: TokenStream): Either<Node.StatementNode.ExpressionNode.ProcCallNode, SourceAnnotation> {
        val identifier = when(val ident = identParser.parse(stream)){
            is Either.Left -> ident.a
            is Either.Right -> return ident
        }
        val lparen = when(val next = stream.next()){
            is Some -> when(next.t){
                is Token.LParenToken -> next
                else -> return buildSourceAnnotation {
                    message = "Expected '(' but instead got ${next.t}"
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
        val args = arrayListOf<Node.StatementNode.ExpressionNode>()
        while(stream.peek is Some && (stream.peek as Some).t !is Token.RParenToken){
            val expr = when(val result = exprParser.parse(stream)){
                is Either.Left -> result.a
                is Either.Right -> return result
            }
            args.add(expr)
            if(stream.peek is Some){
                val next = (stream.peek as Some)
                if(next.t !is Token.CommaToken){
                    if(next.t !is Token.RParenToken){
                        return buildSourceAnnotation {
                            message = "Expected an either ')' or ',' but instead got ${next.t}"
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
                    break
                }
                //Advance the scanner so that we are are passed the ','
                stream.next()
            }
        }
        val rParent = (stream.next() as Some).t as Token.RParenToken
        return Node.StatementNode.ExpressionNode.ProcCallNode(identifier, args, identifier.startPos, rParent.endPos).left()
    }
    companion object{
        val identParser = ParseIdentifier()
        val exprParser = ExpressionParser()
    }
}