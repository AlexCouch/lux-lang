import java.lang.IllegalArgumentException

sealed class VMObject{
    data class VMVariable(val ident: String, val value: VMValue): VMObject(){
        override fun toString(): String = value.toString()
    }
    data class VMFrame(val ident: String): VMObject()
    sealed class VMValue: VMObject(){
        data class VMInteger(val int: Int): VMValue(){
            override fun toString(): String = int.toString()
        }
        object VMUnit: VMValue(){
            override fun toString(): String = "Unit"
        }
        data class VMReference(val variable: VMVariable): VMValue()
    }
}

class VM(val moduleName: String){
    /**
     * Where all objects declared with `var` and `const` go. Regardless of whether they are in a procedure or not
     *
     * If a `const` is declared inside a procedure, it will be preallocated globally but only access from where it's declared.
     *
     * def f:
     *   const x = 5 //Goes on the heap, preallocated globally, only accessed from within `f`
     */
    val heap = arrayListOf<VMObject>()
    /**
     * Where all objects declared with `let` go. These can only be inside a procedure, and are passed by value rather than by reference like var.
     *
     * ```
     * def a(x):
     *   print x
     *
     * def f:
     *   let x = 5 //Goes on the stack, dropped at the end of this procedure's scope
     *   a(x) //x is moved into `a`, thus allowing `a`'s scope to own `x`, thus is dropped at the end of `a`'s scope
     * ```
     */
    val stack = arrayListOf<VMObject>()

    val currentFrame get() = stack.reversed().find { it is VMObject.VMFrame } as? VMObject.VMFrame

    fun findReference(name: String): VMObject.VMValue.VMReference? =
        (heap.find {
            it is VMObject.VMVariable && it.ident == name
        } as? VMObject.VMVariable)?.let {
            VMObject.VMValue.VMReference(it)
        }

    fun findLocal(name: String): VMObject? =
        stack.find {
            it is VMObject.VMVariable && it.ident == name
        } as? VMObject.VMVariable

    fun evalBinaryExpr(expr: Node.StatementNode.ExpressionNode.BinaryNode): VMObject.VMValue =
        when(expr){
            is Node.StatementNode.ExpressionNode.BinaryNode.BinaryAddNode -> {
                val left = evalExpr(expr.left)
                if(left !is VMObject.VMValue.VMInteger){
                    throw RuntimeException("Type Error: Left hand operand of add operator is not int: ${expr.left}")
                }
                val right = evalExpr(expr.right)
                if(right !is VMObject.VMValue.VMInteger){
                    throw RuntimeException("Type Error: Right hand operand of add operator is not int: ${expr.right}")
                }
                VMObject.VMValue.VMInteger(left.int + right.int)
            }
            is Node.StatementNode.ExpressionNode.BinaryNode.BinaryMinusNode -> {
                val left = evalExpr(expr.left)
                if(left !is VMObject.VMValue.VMInteger){
                    throw RuntimeException("Type Error: Left hand operand of minus operator is not int: ${expr.left}")
                }
                val right = evalExpr(expr.right)
                if(right !is VMObject.VMValue.VMInteger){
                    throw RuntimeException("Type Error: Right hand operand of minus operator is not int: ${expr.right}")
                }
                VMObject.VMValue.VMInteger(left.int - right.int)
            }
            is Node.StatementNode.ExpressionNode.BinaryNode.BinaryMultNode -> {
                val left = evalExpr(expr.left)
                if(left !is VMObject.VMValue.VMInteger){
                    throw RuntimeException("Type Error: Left hand operand of multiply operator is not int: ${expr.left}")
                }
                val right = evalExpr(expr.right)
                if(right !is VMObject.VMValue.VMInteger){
                    throw RuntimeException("Type Error: Right hand operand of multiply operator is not int: ${expr.right}")
                }
                VMObject.VMValue.VMInteger(left.int * right.int)
            }
            is Node.StatementNode.ExpressionNode.BinaryNode.BinaryDivNode -> {
                val left = evalExpr(expr.left)
                if(left !is VMObject.VMValue.VMInteger){
                    throw RuntimeException("Type Error: Left hand operand of divide operator is not int: ${expr.left}")
                }
                val right = evalExpr(expr.right)
                if(right !is VMObject.VMValue.VMInteger){
                    throw RuntimeException("Type Error: Right hand operand of divide operator is not int: ${expr.right}")
                }
                VMObject.VMValue.VMInteger(left.int / right.int)
            }
        }

    fun evalExpr(expr: Node.StatementNode.ExpressionNode): VMObject.VMValue {
        return when(expr) {
            is Node.StatementNode.ExpressionNode.IntegerLiteralNode -> {
                VMObject.VMValue.VMInteger(expr.int)
            }
            is Node.StatementNode.ExpressionNode.ReferenceNode -> {
                when(val found = findLocal(expr.refIdent.str)){
                    is VMObject.VMVariable -> found.value
                    is VMObject.VMValue.VMReference -> found.variable.value
                    else -> {
                        val ref = findReference(expr.refIdent.str)
                        ref?.variable?.value ?: throw IllegalStateException("Could not find variable with name ${expr.refIdent.str}")
                    }
                }
            }
            is Node.StatementNode.ExpressionNode.BinaryNode -> evalBinaryExpr(expr)
            is Node.StatementNode.ExpressionNode.ProcCallNode -> {
                var moduleNode: Node.ModuleNode? = null
                expr.walkParents {
                    if(it is Node.ModuleNode) moduleNode = it
                }
                val proc = moduleNode?.statements?.find { stmt ->
                    stmt is Node.StatementNode.DefProcNode && stmt.ident.str == expr.refIdent.str
                } as? Node.StatementNode.DefProcNode ?: throw IllegalStateException("Procedure ${expr.refIdent.str} isn't found. Possibly undeclared.")
                expr.arguments.forEach {
                    stack += evalExpr(it)
                }
                evalProc(proc)
            }
            is Node.StatementNode.ExpressionNode.BlockNode -> TODO()
            is Node.StatementNode.ExpressionNode.ConditionalBranchingNode.BinaryConditionalNode -> TODO()
            is Node.StatementNode.ExpressionNode.ConditionalBranchingNode.WhenConditionalNode -> TODO()
            else -> TODO()
        }
    }

    fun evalProc(procNode: Node.StatementNode.DefProcNode): VMObject.VMValue{
        stack.add(VMObject.VMFrame(procNode.ident.str))
        val values = stack.slice(stack.size - procNode.params.size - 1 until stack.size - 1)
        values.withIndex().forEach { (i, it) ->
            if(it !is VMObject.VMValue) throw IllegalArgumentException("Expected a value for argument ${procNode.params[i].ident.str} but instead got $it")
            val idx = stack.indexOf(it)
            stack.removeAt(idx)
            stack.add(idx, VMObject.VMVariable(procNode.params[i].ident.str, it))
        }
        procNode.body.forEach {
            evalStatement(it)
        }
        val frameIndex = stack.indexOfLast { it is VMObject.VMFrame && it.ident == procNode.ident.str }
        stack.subList(frameIndex, stack.size - 1).clear()
        return VMObject.VMValue.VMUnit
    }

    fun evalStatement(stmt: Node.StatementNode){
        when(stmt){
            is Node.StatementNode.VarNode -> {
                val value = evalExpr(stmt.expression)
                heap += VMObject.VMVariable(stmt.identifier.str, value)
            }
            is Node.StatementNode.LetNode -> {
                val value = evalExpr(stmt.expression)
                stack += VMObject.VMVariable(stmt.identifier.str, value)
            }
            is Node.StatementNode.PrintNode -> {
                val value = evalExpr(stmt.expr)
                println(value)
            }
            is Node.StatementNode.ReassignmentNode -> {
                val value = evalExpr(stmt.expr)
                stack.replaceAll {  obj ->
                    if(obj is VMObject.VMVariable){
                        if(obj.ident == stmt.ident.str){
                            VMObject.VMVariable(obj.ident, value)
                        }else{
                            obj
                        }
                    }else{
                        obj
                    }
                }
            }
            is Node.StatementNode.ExpressionNode -> stack += evalExpr(stmt)
        }
    }

    fun start(moduleNode: Node.ModuleNode){
        stack.add(VMObject.VMFrame(moduleName))
        moduleNode.statements.forEach {
            evalStatement(it)
        }
    }
}