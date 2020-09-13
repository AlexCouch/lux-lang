package parser

import Node
import TokenStream
import arrow.core.Either
import errors.SourceAnnotation

interface ParseRule{
    fun parse(stream: TokenStream): Either<Node, SourceAnnotation>
}
interface StatementParseRule: ParseRule