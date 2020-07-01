
sealed class Node(open val pos: TokenPos){
    internal var parent: Node? = null

    data class ModuleNode(val ident: IdentifierNode, val statements: ArrayList<StatementNode>): Node(TokenPos.default){
        override fun assignParents() {
            for(statement in this.statements){
                statement.parent = this
                statement.assignParents()
            }
        }

        override fun toString(): String = buildPrettyString{
            appendWithNewLine("Module{")
            indent {
                statements.forEach {
                    appendWithNewLine("$it")
                }
            }
            append("}")
        }

        override fun walkChildren(block: (Node) -> Unit) {
            block(ident)
            statements.forEach(block)
        }
    }
    data class IdentifierNode(val str: String, override val pos: TokenPos): Node(pos){
        override fun assignParents() {}

        override fun toString(): String = buildPrettyString{
            appendWithNewLine("Identifier{")
            indent {
                appendWithNewLine("lexeme: $str,")
                appendWithNewLine("pos: $pos")
            }
            append("}")
        }
    }
    sealed class StatementNode(override val pos: TokenPos): Node(pos){
        data class VarNode(val identifier: IdentifierNode, val expression: ExpressionNode, val type: IdentifierNode, override val pos: TokenPos): StatementNode(pos){
            override fun assignParents() {
                identifier.parent = this
                expression.parent = this
                expression.assignParents()
            }

            override fun toString(): String = buildPrettyString{
                appendWithNewLine("Var{")
                indent {
                    appendWithNewLine("pos: $pos")
                    appendWithNewLine("ident: $identifier")
                    appendWithNewLine("type: $type")
                    appendWithNewLine("expression: $expression")
                }
                append("}")
            }

            override fun walkChildren(block: (Node) -> Unit) {
                block(identifier)
                block(expression)
            }
        }
        data class LetNode(val identifier: IdentifierNode, val expression: ExpressionNode, val type: IdentifierNode, override val pos: TokenPos): StatementNode(pos){
            override fun assignParents() {
                identifier.parent = this
                expression.parent = this
                expression.assignParents()
            }

            override fun toString(): String = buildPrettyString{
                appendWithNewLine("Let{")
                indent {
                    appendWithNewLine("pos: $pos")
                    appendWithNewLine("ident: $identifier")
                    appendWithNewLine("type: $type")
                    appendWithNewLine("expression: $expression")
                }
                append("}")
            }

            override fun walkChildren(block: (Node) -> Unit) {
                block(identifier)
                block(expression)
            }
        }
        data class ConstNode(
            val identifier: IdentifierNode,
            val expression: ExpressionNode,
            val type: IdentifierNode,
            override val pos: TokenPos
        ): StatementNode(pos){
            override fun assignParents() {
                identifier.parent = this
                expression.parent = this
                expression.assignParents()
            }

            override fun toString(): String = buildPrettyString{
                appendWithNewLine("Const{")
                indent {
                    appendWithNewLine("pos: $pos")
                    appendWithNewLine("ident: $identifier")
                    appendWithNewLine("type: $type")
                    appendWithNewLine("expression: $expression")
                }
                append("}")
            }

            override fun walkChildren(block: (Node) -> Unit) {
                block(identifier)
                block(expression)
            }
        }
        data class PrintNode(val expr: ExpressionNode, override val pos: TokenPos): StatementNode(pos){
            override fun assignParents() {
                expr.parent = this
                expr.assignParents()
            }

            override fun toString(): String = buildPrettyString{
                appendWithNewLine("Print{")
                indent {
                    appendWithNewLine("expr: $expr")
                }
                append("}")
            }

            override fun walkChildren(block: (Node) -> Unit) {
                block(expr)
            }
        }
        data class ReassignmentNode(val ident: IdentifierNode, val expr: ExpressionNode, override val pos: TokenPos): StatementNode(pos){
            override fun assignParents() {
                ident.parent = this
                expr.parent = this
                expr.assignParents()
            }

            override fun toString(): String = buildPrettyString{
                appendWithNewLine("Reassignment{")
                indent {
                    appendWithNewLine("ident: $ident,")
                    appendWithNewLine("expr: $expr")
                }
                append("}")
            }

            override fun walkChildren(block: (Node) -> Unit) {
                block(ident)
                block(expr)
            }
        }

        data class ReturnNode(val expr: ExpressionNode, override val pos: TokenPos): StatementNode(pos) {
            override fun assignParents() {
                expr.parent = this
                expr.assignParents()
            }

            override fun toString(): String =
                buildPrettyString {
                    appendWithNewLine("Return{")
                    indent {
                        appendWithNewLine("expr: $expr")
                        appendWithNewLine("pos: $pos")
                    }
                }
        }

        sealed class ExpressionNode(override val pos: TokenPos): StatementNode(pos){
            data class IntegerLiteralNode(val int: Int, override val pos: TokenPos): ExpressionNode(pos){
                override fun assignParents() {}

                override fun toString(): String = buildPrettyString{
                    appendWithNewLine("Integer{")
                    indent {
                        appendWithNewLine("int: $int,")
                        appendWithNewLine("pos: $pos")
                    }
                    append("}")
                }
            }
            data class ReferenceNode(val refIdent: IdentifierNode, override val pos: TokenPos): ExpressionNode(pos){
                override fun assignParents() {}

                override fun toString(): String = buildPrettyString {
                    appendWithNewLine("Reference{")
                    indent {
                        appendWithNewLine("pos: $pos,")
                        appendWithNewLine("ident: $refIdent")
                    }
                    append("}")
                }

                override fun walkChildren(block: (Node) -> Unit) {
                    block(refIdent)
                }

            }
            data class ProcCallNode(val refIdent: IdentifierNode, val arguments: List<ExpressionNode>, override val pos: TokenPos): ExpressionNode(pos){
                override fun assignParents() {}

                override fun toString(): String = buildPrettyString {
                    appendWithNewLine("ProcCall{")
                    indent {
                        appendWithNewLine("pos: $pos,")
                        appendWithNewLine("ident: $refIdent,")
                        appendWithNewLine("args: [")
                        indent {
                            arguments.forEach {
                                append("$it")
                            }
                        }
                        appendWithNewLine("]")
                    }
                    append("}")
                }

                override fun walkChildren(block: (Node) -> Unit) {
                    block(refIdent)
                    arguments.forEach(block)
                }
            }

            data class BlockNode(val stmts: List<StatementNode>, override val pos: TokenPos): ExpressionNode(pos) {
                override fun assignParents() {
                    stmts.forEach {
                        it.parent = this
                        it.assignParents()
                    }
                }

                override fun toString(): String = buildPrettyString{
                    appendWithNewLine("AnonBlock{")
                    indent {
                        appendWithNewLine("pos: $pos")
                        appendWithNewLine("body: [")
                        indent {
                            stmts.forEach {
                                appendWithNewLine("$it")
                            }
                        }
                        appendWithNewLine("]")
                    }
                    append("}")
                }

                override fun walkChildren(block: (Node) -> Unit) {
                    stmts.forEach(block)
                }
            }
            sealed class BinaryNode(open val left: ExpressionNode, open val right: ExpressionNode, override val pos: TokenPos): ExpressionNode(pos){
                override fun assignParents() {
                    left.parent = this
                    left.assignParents()

                    right.parent = this
                    right.assignParents()
                }

                override fun toString(): String = buildPrettyString{
                    appendWithNewLine("left: $left,")
                    appendWithNewLine("right: $right")
                }

                override fun walkChildren(block: (Node) -> Unit) {
                    block(left)
                    block(right)
                }
                data class BinaryAddNode(override val left: ExpressionNode, override val right: ExpressionNode, override val pos: TokenPos): BinaryNode(left, right, pos){
                    override fun toString(): String = buildPrettyString{
                        appendWithNewLine("BinaryAdd{")
                        indent {
                            append(super.toString())
                        }
                        appendWithNewLine("}")
                    }
                }
                data class BinaryMinusNode(override val left: ExpressionNode, override val right: ExpressionNode, override val pos: TokenPos): BinaryNode(left, right, pos){
                    override fun toString(): String = buildPrettyString{
                        appendWithNewLine("BinaryMinus{")
                        indent {
                            append(super.toString())
                        }
                        appendWithNewLine("}")
                    }
                }
                data class BinaryMultNode(override val left: ExpressionNode, override val right: ExpressionNode, override val pos: TokenPos): BinaryNode(left, right, pos){
                    override fun toString(): String = buildPrettyString{
                        appendWithNewLine("BinaryMult{")
                        indent {
                            append(super.toString())
                        }
                        appendWithNewLine("}")
                    }
                }
                data class BinaryDivNode(override val left: ExpressionNode, override val right: ExpressionNode, override val pos: TokenPos): BinaryNode(left, right, pos){
                    override fun toString(): String = buildPrettyString{
                        appendWithNewLine("BinaryDiv{")
                        indent {
                            append(super.toString())
                        }
                        appendWithNewLine("}")
                    }
                }
            }
        }
        data class DefProcNode(
            val ident: IdentifierNode,
            val params: ArrayList<ProcParamNode>,
            val body: ArrayList<StatementNode>,
            val returnType: IdentifierNode,
            override val pos: TokenPos
        ): StatementNode(pos) {

            override fun assignParents() {
                ident.parent = this
                params.forEach {
                    it.parent = this
                    it.assignParents()
                }
            }

            override fun toString(): String = buildPrettyString{
                appendWithNewLine("DefProc{")
                indent {
                    appendWithNewLine("pos: $pos,")
                    appendWithNewLine("ident: $ident,")
                    appendWithNewLine("params: [")
                    indent {
                        params.forEach {
                            appendWithNewLine("$it")
                        }
                    }
                    appendWithNewLine("],")
                    appendWithNewLine("body: [")
                    indent {
                        body.forEach{
                            appendWithNewLine("$it")
                        }
                    }
                    appendWithNewLine("]")
                }
                append("}")
            }

            override fun walkChildren(block: (Node) -> Unit) {
                block(ident)
                params.forEach(block)
                body.forEach(block)
            }
        }

        data class ProcParamNode(val ident: IdentifierNode, val type: IdentifierNode, override val pos: TokenPos): StatementNode(pos) {
            override fun assignParents() {
                ident.parent = this
            }

            override fun toString(): String = buildPrettyString{
                appendWithNewLine("Param{")
                indent {
                    appendWithNewLine("pos: $pos,")
                    appendWithNewLine("ident: $ident")
                }
                append("}")
            }

            override fun walkChildren(block: (Node) -> Unit) {
                block(ident)
            }
        }
    }

    abstract fun assignParents()

    fun walkParents(block: (Node) -> Unit){
        if(this.parent != null){
            block(this.parent!!)
            this.parent!!.walkParents(block)
        }
    }

    fun findNode(predicate: (Node) -> Boolean): Node?{
        var found: Node? = null
        walkParents { parent ->
            parent.walkChildren { child ->
                if(predicate(child)){
                    found = child
                }
            }
        }
        return found
    }

    open fun walkChildren(block: (Node) -> Unit){}

    fun first(predicate: (Node) -> Boolean): Node?{
        var found: Node? = null
        walkChildren{
            if(predicate(it)){
                found = it
            }
        }
        return found
    }
}

fun Token.IdentifierToken.toIdentifierNode() = Node.IdentifierNode(this.lexeme, this.pos)