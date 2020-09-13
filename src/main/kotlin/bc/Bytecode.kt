package bc

enum class Bytecode{
    /**
     * Push a name to be bound to the object on the top of the stack. This then creates a variable on the stack,
     * which may be read from or written to. This takes an index to the names store.
     *
     * [PUSH_NAME] nameidx
     *
     * This will push a new name object to the top of the stack, which is immediately bound to the value at the top of
     * the stack.
     *
     */
    PUSH_NAME,

    /**
     * This will create an object on the heap. It is not required for an object to be on the heap, but typically, this
     * is bound to some kind of variable object on the stack.
     */
    HEAP,

    /**
     * A stack variable allocation.
     * This will create an object on the stack. References to this cannot be made but instead must be done through
     * observers.
     *
     * WARNING: Observers have not been implemented yet!! -AC 8/17
     *
     * An observer is a bytecode transformer that is written in source code, but is treated as a compiletime transformer
     * so that the bytecode generator is influenced to produce a different set of opcodes as opposed to the standard
     * READ instructions that are always generated without an observer.
     *
     * Note: References created by [REF] have observers built in, called RefObserver, which allows for the use of
     * references for reading a value. They are always consumed and replaced with a new reference. In fact, to improve
     * performance, they will never be consumed.
     */
    STACK,

    /**
     * Reads from a given variable. The first operand of this opcode is the index of the name being referenced.
     *
     * The result of this opcode is the object bound to the given name is moved to the top of the stack, where any
     * subsequent instruction will use it. Any interactive instruction (arithmetic instructions, swap, write, etc) will
     * result in the immediate destruction of the bound object. To avoid this, it recommended that bytecode generators
     * generate alternative instructions depending on the situation. This gives any bytecode generator the opportunity
     * to implement features not bound to the original language. So classes are not required, and neither are functions,
     * procedures, etc.
     *
     * Example:
     * READ 1 ;Move the object bound to name index 1 to the top of the stack
     * READ 2 ;Move the object bound to name index 2 to the top of the stack
     * ADD    ;Add them together, thus destroying both the objects and replacing them with a new one, their sum
     */
    READ,

    /**
     * This is a four step opcode. This will use an initial READ followed by an object acquisition (the process of
     * acquiring an object onto the top of the stack, whether it'd be a new push, a [READ], a [DUP], etc) and then
     * SWAP-ping the bound object with the unbound object. Lastly, it pops the previously bound object off the stack.
     *
     * ```
     * let x = 5
     * x = 10 #Instructions below are on this line
     * ```
     *
     * Step1: READ 0        ;0 is the index where the name 'x' is placed.
     * Step2: STACK CONST 1 ;1 is the index in the constant pool where 10 is placed. 0 is 5
     * Step3: SWAP
     * Step4: POP
     *
     * The result is the variable 'x' bound to integer '5' will be READ (moved to the top of the stack), and swapped with
     * a newly pushed constant integer '10'. The two objects are swapped so that 'x' is now bound to '10' and '5' is
     * popped off the stack, due to the initial READ instruction on that bound '5' object.
     */
    WRITE,

    //              #Expressions

    /**
     * References an object by creating a new Reference object, and pushing it onto the stack.
     * The only way the Reference object can be on the heap is if it's bound to a heap variable (const or var).
     * For it to be on the heap, it must be preceded by [HEAP], otherwise, it goes on the stack.
     *
     * References are always used when referencing heap variables (const, var). Any time a read/write is made on one,
     * it always goes through a Reference. When a const/var is made, it's assigned value is a Reference to the
     * heap object.
     *
     * This opcode is always proceeded by an index in the name store for the variable being referenced.
     * [REF] nameidx
     */
    REF,

    /**
     * Duplicate the object bound to the given name. The first operand of this opcode is an index into the name store
     * of the variable being duplicated.
     *
     * Some observations will transform a variable's READ into other instructions. It's possible for an observer to
     * generate a [DUP] opcode instead of a [READ].
     *
     * [DUP] nameidx
     */
    DUP,

    /**
     * Tell the virtual machine to push a new frame onto the stack. All things with bodies, such as conditionals,
     * procedures, etc use a block internally. This keeps things simple so that the same logic for executing code inside
     * a procedure can be used for executing code inside an explicit block. Anything that has "a block of code" will
     * require a new block to be pushed onto the stack. Blocks are not necessarily objects, but are instead slices of
     * the stack, where all objects pushed onto the stack directly under this block will be consume when the block
     * is consume.
     */
    BLOCK,

    ADD,
    SUB,
    MUL,
    DIV,

    //              #JUMPS
    /**
     * Static jump. This will always result in jumping to another block of code.
     * This is used in procedure calls, when we generate during codegen the location of the procedure's bytecode index,
     * and use it to statically jump to the start of the procedure's body. This is also used for statically jumping
     * passed alternative branches of a conditional at the end of the consequence branch.
     *
     * Procedure calls use this along side other instructions to give the effect of calling a procedure.
     *
     * Step1: A new integer constant object is pushed to the top of the stack.
     * Step2: A new frame object is pushed to the top of the stack, where all subsequent objects are dependent on.
     *          When this frame gets popped at the end of the procedure, all the child objects of the frame are popped
     *          as well.
     * Step3: The arguments for the procedure are put at the top of the stack.
     * Step4: A static jump to the start of the procedure is invoked.
     * Step5 (optional): If there are parameters, then new [PUSH_NAME] instructions are invoked.
     *
     * ```
     *  def average(x: int, y: int, z: int) -> int:
     *      let sum = x + y + z
     *      return sum / 3
     *
     *  const result = average(5, 3, 6)
     * ```
     * Results in:
     * ```
     *  NAMES:
     *  0: average
     *  1: x
     *  2: y
     *  3: z
     *  4: sum
     *  5: result
     *  6: __average_call_return_site__ ;Generated internally to keep track of where to return to after average procedure finishes
     *  7: __0__
     *
     *  CONSTANTS:
     *  0: 3
     *  1: 5
     *  2: 3
     *  3: 6
     *  4: 0023
     *
     *  0001    PUSH_NAME   1       ;Push name for parameter 'x'
     *  0002    PUSH_NAME   2       ;Push name for parameter 'y'
     *  0003    PUSH_NAME   3       ;Push name for parameter 'z'
     *  0004    READ        2       ;Read parameter 'y'
     *  0005    READ        3       ;Read parameter 'z'
     *  0006    ADD                 ;Add 'y' and 'z'
     *  0007    PUSH_NAME   7       ;Push name for variable '__0__' which is a temp variable for y + z
     *  0008    READ        1       ;Read parameter 'x'
     *  0009    READ        7       ;Read '__0__' which hold the temporary value of y + z
     *  0010    ADD                 ;Add __0__ and x (x + %0)
     *  0011    PUSH_NAME   4       ;Push name for variable 'result'. Bind it to the top of the stack (x + %0)
     *  0012    CONSTANT    0       ;Push constant int '3'
     *  0013    READ        4       ;Read from variable 'result'
     *  0014    DIV                 ;Divide 'result' by constant int '3' and push to the top of the stack
     *  0015    READ        6       ;Read the internal variable in name store at index 6
     *  0016    JUMP        TOP     ;Use the top of the stack for the jump target. This would be bound to name index 6.
     *  0017    CONSTANT    4       ;Push the value of the procedure callsite so we know where to jump back to
     *  0018    PUSH_NAME   6       ;Bind the jump target (instruction 0013) to name 6. This will be read from at the end of the 'average' procedure.
     *  0019    BLOCK               ;Push a new frame for the called procedure. See [BLOCK]
     *  0020    CONSTANT    1       ;Push the constant at index 1, so it can be bound to the first parameter
     *  0021    CONSTANT    2       ;Push the constant at index 2, so it can be bound to the second parameter
     *  0022    CONSTANT    3       ;Push the constant at index 3, so it can be bound to the third parameter
     *  0023    JUMP        0001    ;Jump to instruction 0001 to start running the procedure
     *  0024    PUSH_NAME   5       ;Bind the top of the stack to variable name at index 5 ('result')
     *  ```
     */
    JUMP,

    /**
     * Jump if the top item of the stack is Truthy. Truthy means either it exists or it is True.
     *
     * ```
     *  const result = average(5, 3, 6)
     *  if result > 10:
     *      print "Average of 5, 3, and 6 is greater than 10!"
     *  else:
     *      print "Oh, well!"
     * ```
     * Results in:
     * ```
     *  NAMES:
     *  0: result
     *  1: __average_call_return_site__ ;Generated internally to keep track of where to return to after average procedure finishes
     *
     *  CONSTANT
     *  0: "Average of 5, 3, and 6 is greater than 10!"
     *  1: "Oh, well!"
     *  2: 10
     *  3: 5
     *  4: 3
     *  5: 6
     *  6: 0008
     *
     *  0001    CONSTANT    6       ;Push the value of the procedure callsite so we know where to jump back to
     *  0002    PUSH_NAME   6       ;Bind the jump target (instruction 0013) to name 6. This will be read from at the end of the 'average' procedure.
     *  0003    BLOCK               ;Push a new frame for the called procedure
     *  0004    CONSTANT    1       ;Push the constant at index 1, so it can be bound to the first parameter
     *  0005    CONSTANT    2       ;Push the constant at index 2, so it can be bound to the second parameter
     *  0006    CONSTANT    3       ;Push the constant at index 3, so it can be bound to the third parameter
     *  0007    JUMP        0001    ;Jump to instruction 0001 to start running the procedure
     *  0008    PUSH_NAME   5       ;Bind the top of the stack to variable name at index 5 ('result')
     *  0009    CONSTANT    2       ;Push the constant int '10' for truthy comparison
     *  0010    GREATER             ;Check if the object on the stack at indices n-2 (topmost) is greater than n-1 (second to topmost)
     *  0011    JTRU        0015    ;Jump if the object on the top of the stack is Truthy
     *  0012    CONSTANT    0       ;Push the string constant at index 0
     *  0013    PRINT               ;Print the top of the stack
     *  0014    RET                 ;Terminate the program
     *  0015    CONSTANT    1       ;Push the string constant at index 1
     *  0016    PRINT               ;Print out the top of the stack
     * ```
     */
    JTRU,

}