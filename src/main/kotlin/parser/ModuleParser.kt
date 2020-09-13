package parser

import TokenStream
import arrow.core.*
import errors.ErrorHandling
import errors.SourceAnnotation
import errors.buildSourceAnnotation

class ModuleParser(val moduleName: String, val errorHandler: ErrorHandling){
    fun parse(stream: TokenStream): Option<Node.ModuleNode> {
        val statements = arrayListOf<Node.StatementNode>()
        while(stream.peek !is None){
            when(val statement = statementParser.parse(stream)) {
                is Either.Left -> statements.add(statement.a)
                is Either.Right -> {
                    errorHandler.error {
                        message = "An error occurred while parsing module"
                        addAnnotation(statement.b)
                    }
                    return none()
                }
            }
        }
        return Node.ModuleNode(Node.IdentifierNode(moduleName, TokenPos.default, TokenPos.default), statements).some()
    }

    companion object{
        val statementParser = StatementParser()
    }

}