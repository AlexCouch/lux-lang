import javax.swing.plaf.nimbus.State

sealed class Node(open val pos: TokenPos){
    internal var parent: Node? = null

    data class ModuleNode(val statements: ArrayList<StatementNode>): Node(TokenPos.default){
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
        data class VarNode(val identifier: IdentifierNode, val expression: ExpressionNode, override val pos: TokenPos): StatementNode(pos){
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
                    appendWithNewLine("expression: $expression")
                }
                append("}")
            }
        }
        data class LetNode(val identifier: IdentifierNode, val expression: ExpressionNode, override val pos: TokenPos): StatementNode(pos){
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
                    appendWithNewLine("expression: $expression")
                }
                append("}")
            }
        }
        data class ConstNode(val identifier: IdentifierNode, val expression: ExpressionNode, override val pos: TokenPos): StatementNode(pos){
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
                    appendWithNewLine("expression: $expression")
                }
                append("}")
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
        data class DefProcNode(val ident: IdentifierNode, val params: ArrayList<ProcParamNode>, val body: ArrayList<StatementNode>, override val pos: TokenPos): StatementNode(pos) {
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
        }

        data class ProcParamNode(val ident: IdentifierNode, override val pos: TokenPos): Node(pos) {
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
        }
    }

    abstract fun assignParents()

    fun walkParents(block: (Node) -> Unit){
        if(this.parent != null){
            block(this.parent!!)
            this.parent!!.walkParents(block)
        }
    }
}

fun Token.IdentifierToken.toIdentifierNode() = Node.IdentifierNode(this.lexeme, this.pos)