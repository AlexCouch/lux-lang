package parser

import Node
import Position
import Token
import TokenPos
import TokenStream
import arrow.core.Either
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import errors.SourceAnnotation
import errors.buildSourceAnnotation

class ParseIdentifier: ParseRule{
    override fun parse(stream: TokenStream): Either<Node.IdentifierNode, SourceAnnotation> {
        val next = stream.next()
        return if(next is Some){
            if(next.t is Token.IdentifierToken){
                Node.IdentifierNode((next.t as Token.IdentifierToken).lexeme, next.t.startPos, next.t.endPos).left()
            }else {
                buildSourceAnnotation {
                    message = "Expected an identifier but instead got ${next.t}"
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
            buildSourceAnnotation {
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
}