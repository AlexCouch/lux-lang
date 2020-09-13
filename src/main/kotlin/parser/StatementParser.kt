package parser

import Node
import Position
import TokenPos
import TokenStream
import arrow.core.Either
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import errors.SourceAnnotation
import errors.buildSourceAnnotation

class StatementParser: ParseRule{
    override fun parse(stream: TokenStream): Either<Node.StatementNode, SourceAnnotation> {
        stream.checkpoint()
        val constParseResult = constParser.parse(stream)
        if(constParseResult is Either.Left){
            return constParseResult
        }
        stream.reset()
        val exprResult = exprParser.parse(stream)
        if(exprResult is Either.Left){
            return exprResult
        }
        val printResult = printParser.parse(stream)
        if(printResult is Either.Left){
            return printResult
        }
        if(stream.current is Some){
            val token = (stream.current as Some).t
            return buildSourceAnnotation {
                message = "No recognized statement at token: $token"
                errorLine {
                    start = token.startPos
                    end = token.endPos
                }
                sourceOrigin {
                    start = TokenPos(Position(token.startPos.pos.line, 0), token.startPos.offset - token.startPos.pos.col, token.startPos.indentLevel)
                    end = token.endPos
                    source = stream.input
                }
            }.right()
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
    }

    companion object{
        val constParser = ParseConst()
        val exprParser = ExpressionParser()
        val printParser = PrintParser()
    }

}