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
        val varParseResult = varParser.parse(stream)
        if(varParseResult is Either.Left){
            return varParseResult
        }
        stream.reset()
        val legacyVarParseResult = legacyVarParser.parse(stream)
        if(legacyVarParseResult is Either.Left){
            return legacyVarParseResult
        }
        stream.reset()
        val printResult = printParser.parse(stream)
        if(printResult is Either.Left){
            return printResult
        }
        stream.reset()
        val procParseResult = procParser.parse(stream)
        if(procParseResult is Either.Left){
            return procParseResult
        }
        stream.reset()
        val retParseResult = retParser.parse(stream)
        if(retParseResult is Either.Left){
            return retParseResult
        }
        stream.reset()
        val exprResult = exprParser.parse(stream)
        if(exprResult is Either.Left){
            return exprResult
        }
        stream.popCheckpoint()
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
        val varParser = VarParser()
        val legacyVarParser = LegacyVarParser()
        val exprParser = ExpressionParser()
        val printParser = PrintParser()
        val procParser = ParseProcedure()
        val retParser = ParseReturn()
    }

}