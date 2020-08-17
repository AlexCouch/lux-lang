package bc

enum class Bytecode{
    /**
     * Declare a variable, whether on the stack or heap has to do with the proceeding bytecode instructions.
     * This instruction is a context switcher, and tells the vm to switch to the variable declaration context.
     *
     * Varables come in two parts:
     *      - Variable Object
     *          - This is the header of the object, which contains the variable's signature, source location, etc.
     *          - This will also be encoded in bytecode as such:
     *              [VAR] (var_name) [TYPE] (type_name) ([HEAP|STACK]) [ASSIGNMENT]
     *      - Assigned Expression
     *          - See #Expressions
     *
     */
    VAR,

    /**
     * This will create an object on the heap. It is not required for an object to be on the heap, but typically, this
     * is bound to some kind of variable object on the stack.
     */
    HEAP,

    /**
     * A stack variable allocation.
     * This will create an object on the stack. To reference this, observers are required to read without destroying the object.
     * See [OBSERVE]
     */
    STACK,

    /**
     * Used to read from an object on the stack without consume it. This will take some observer object as an argument,
     * and that observer is a procedure that will tell the VM how to observe the given object, as the second argument.
     *
     * [OBSERVE] observer varident
     */
    OBSERVE,

    /**
     * A [VAR] subinstruction, used to declare the assignment of a variable.
     * This sbcontext makes it clearer and faster to create and verify variable objects.
     *
     * [VAR] ;see VAR; [ASSIGNMENT] expression
     */
    ASSIGNMENT,

    /**
     * A [VAR] subinstruction, used to declare a new variable that replaces the subject with a new version of itself.
     * This subcontext is used to mutate the stack or heap contents that will pull the given [VAR] object to the top
     * of the stack, and change its assigned expression.
     *
     * For heap variables, this will pull the variable's object to the top of the stack, then replace itself assigned
     * pointer to a new pointer on the heap. During this, the original pointer will be used to consume the original object
     * on the heap such that it is used during the transformation.
     *
     * A variable object whose assigned object is an Int(5). If we are to mutate this as an additive transformation,
     * we would get the morphisms:
     *      A : T
     *      B : A
     *      C : A + B
     *      A -> B -> C
     *          where A + B yields a new object, C,
     *              whose composition can only be described as an adjoint relation between A and B
     *              For Natural or Real numbers, this yields the following typed morphism:
     *              A -> A -> A
     *              Because the type of A, B, and C are all of type A.
     *              A ~= B ~= B
     *              This also yield the following type triad:
     *                      A
     *                     / \
     *                    B - C
     *              This is because the types of A, B, and C are all the same type or polymorphic to each other.
     *              The consumption of Object A during the construction of object B, not only consumes A but also consumes B.
     *              The consumption morphism looks like this:
     *              A -> B -> C -> Neg B -> Neg A
     *
     *  [MUT] ident expression
     */
    MUT,

    /**
     * Declare a procedure, which is then encapsulated into a ProcBox, which contains other information such as:
     *  - Source Location
     *  - Deserialized Signature -- The signature is in bytecode form, and is deserialized so that it's easier to interpret on demand.
     *  - Body
     *
     *  [PROC] source_location ([PARAM] pident [TYPE] tyident)* [TYPE] retype [BLOCK]
     */
    PROC,

    /**
     * The start of a procedure parameter. See [PROC] for parameter signature.
     * The VM will take the index of the param on the stack and bind it to the parameter.
     *
     * If there are 3 parameters, as a list called P, and the VM stack called S, and a top index (len(S)-1) called T,
     * when T = 10,
     * then P[0] = S[T-(3-1)+0] => S[T-2+0] => S[T-2] => S[8]
     *      P[1] = S[T-(3-1)+1] => S[T-2+1] => S[T-1] => S[9]
     *      P[2] = S[T-(3-1)+2] => S[T-2+2] => S[T]   => S[10]
     *      and so forth
     * ergo,
     *      P[i] = S[T-(len(P)-1)+i]
     */
    PARAM,


    /**
     * Use to indicate a type annotation, which will then be used to describe the construct at hand.
     * This is used in VAR and PROC to specify what type something is.
     * For VAR it's the assignment, for PROC is the return expression
     */
    TYPE,

    //              #Expressions

    /**
     * An expressive instruction used to acquire a new reference to a specified object.
     * The result depends on whether the variable's assigned object is on the heap or on the stack.
     * If it's on the heap, a new reference object will be made and pushed to the top of the stack.
     * If it's on the stack, that object will be moved to the top of the stack.
     *
     * [REF] refident
     */
    REF,

    /**
     * Tell the virtual machine to push a new frame onto the stack. All things with bodies, such as conditionals,
     * procedures, etc use a block internally. This keeps things simple so that the same logic for executing code inside
     * a procedure can be used for executing code inside an explicit block. Anything that has "a block of code" will
     * require a new block to be pushed onto the stack. Blocks are not necessarily objects, but are instead slices of
     * the stack, where all objects pushed onto the stack directly under this block will be consume when the block
     * is consume.
     */
    BLOCK,

    CALL,

    ADD,
    SUB,
    MUL,
    DIV,

    //              #JUMPS
    /**
     * Static jump. This will always result in jumping to another block of code.
     * This is used in procedure calls, when we load the called procedure's metadata, and use it to statically jump
     * to the start of the procedure's body. This is also used for jumping back to the caller after a return.
     *
     * See [CALL] and [RETURN] for more info.
     */
    JUMP,

    /**
     * Jump if the top item of the stack is Truthy. Truthy means either it exists or it is True
     */
    JTRU,

}