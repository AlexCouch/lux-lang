package parser

import Node
import TokenStream
import arrow.core.*
import errors.SourceAnnotation
import errors.buildSourceAnnotation
import toIdentifierNode

class ParseProcedure: StatementParseRule{
    override fun parse(stream: TokenStream): Either<Node.StatementNode.DefProcNode, SourceAnnotation> {
        val def = when(val next = stream.next()){
            is Some -> if(next.t is Token.IdentifierToken){
                val t = next.t as Token.IdentifierToken
                if(t.lexeme == "def"){
                    t
                }else{
                    return buildSourceAnnotation {
                        message = "Expected a 'def' token but instead got $t"
                        errorLine {
                            start = t.startPos
                            end = t.endPos
                        }
                        sourceOrigin {
                            start = t.startPos
                            end = t.endPos
                            source = stream.input
                        }
                    }.right()
                }
            }else{
                return buildSourceAnnotation {
                    message = "Expected a 'def' token but instead got ${next.t}"
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
        val ident = when(val ident = identParser.parse(stream)){
            is Either.Right -> return ident
            is Either.Left -> ident.a
        }
        val lPar = when(val next = stream.next()){
            is Some -> if(next.t !is Token.LParenToken){
                return buildSourceAnnotation {
                    message = "Expected a '(' token but instead got ${next.t}"
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
            }else{
                next.t as Token.LParenToken
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
        val params = arrayListOf<Node.StatementNode.ProcParamNode>()
        while(stream.peek is Some && (stream.peek as Some).t !is Token.RParenToken){
            val paramIdent = when(val paremtIdent = identParser.parse(stream)){
                is Either.Right -> return paremtIdent
                is Either.Left -> paremtIdent.a
            }
            val colon = when(val next = stream.next()){
                is Some -> if(next.t !is Token.ColonToken){
                    return buildSourceAnnotation {
                        message = "Expected a ':' token but instead got ${next.t}"
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
                }else{
                    next.t as Token.ColonToken
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
            val tyident = when(val tyident = identParser.parse(stream)){
                is Either.Right -> return tyident
                is Either.Left -> tyident.a
            }
            params += Node.StatementNode.ProcParamNode(paramIdent, tyident, paramIdent.startPos, tyident.endPos)
            if(stream.peek is Some && (stream.peek as Some).t !is Token.CommaToken){
                break
            }
            //Advance the scanner so that we are passed the ','
            stream.next()
        }
        //Advance the scanner so that we are passed the ')'
        stream.next()
        val retType = when(val next = stream.peek){
            is Some -> if(next.t is Token.HyphenToken){
                stream.next() //To line up with the '-' so that '>' is next
                when(val next = stream.next()){
                    is Some -> if(next.t is Token.RAngleToken) {
                        when(val next = stream.next()){
                            is Some -> if(next.t is Token.IdentifierToken){
                                (next.t as Token.IdentifierToken).toIdentifierNode()
                            }else{
                                Node.IdentifierNode("dyn", TokenPos.default, TokenPos.default)
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
                    }else{
                        Node.IdentifierNode("dyn", TokenPos.default, TokenPos.default)
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
            }else{
                Node.IdentifierNode("dyn", TokenPos.default, TokenPos.default)
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
        if(stream.peek is Some){
            val peek = (stream.peek as Some)
            if(peek.t.startPos.pos.line > def.startPos.pos.line && peek.t.startPos.indentLevel > def.startPos.indentLevel){
                val statements = arrayListOf<Node.StatementNode>()
                while((stream.peek is Some) && (stream.peek as Some).t.startPos.indentLevel > def.startPos.indentLevel){
                    when(val statement = statementParser.parse(stream)){
                        is Either.Left -> statements.add(statement.a)
                        is Either.Right -> return statement
                    }
                }
                return Node.StatementNode.DefProcNode(ident, params, statements, retType, def.startPos, statements.last().endPos).left()
            }
            return buildSourceAnnotation {
                message = "Expected a body for procedure that is at least 1 indent level greater than the procedure definition, but instead found token: ${peek.t}"
                errorLine {
                    start = peek.t.startPos
                    end = peek.t.endPos
                }
                sourceOrigin {
                    start = peek.t.startPos
                    end = peek.t.endPos
                    source = stream.input
                }
            }.right()
        }
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
    companion object{
        val identParser = ParseIdentifier()
        val statementParser = StatementParser()
    }
}

class ParseReturn: StatementParseRule{
    override fun parse(stream: TokenStream): Either<Node.StatementNode.ReturnNode, SourceAnnotation> {
        val ret = when(val ident = identParser.parse(stream)){
            is Either.Left -> ident.a
            is Either.Right -> return ident
        }
        if(ret.str != "return"){
            return buildSourceAnnotation {
                message = "Expected a 'return' but instead got: $ret"
                errorLine {
                    start = ret.startPos
                    end = ret.endPos
                }
                sourceOrigin {
                    start = ret.startPos
                    end = ret.endPos
                    source = stream.input
                }
            }.right()
        }
        val expr = when(val expr = exprParser.parse(stream)){
            is Either.Left -> expr.a
            is Either.Right -> return expr
        }
        return Node.StatementNode.ReturnNode(expr, ret.startPos, expr.endPos).left()
    }
    companion object{
        val identParser = ParseIdentifier()
        val exprParser = ExpressionParser()
    }
}