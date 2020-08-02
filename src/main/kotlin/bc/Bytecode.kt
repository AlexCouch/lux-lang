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
     *              [VAR] (var_name) [TYPE] (type_name) ([HEAP|STACK] [ASSIGNMENT]
     *
     * Subinstructions:
     *  [HEAP]
     *  [STACK]
     *  [ASSIGNMENT]
     */
    VAR,

    /**
     * A heap variable allocation.
     * This will create the variable's assignment on the heap, but push the variable object to the stack.
     */
    HEAP,

    STACK,

    /**
     * A [VAR] subinstruction, used to declare the assignment of a variable.
     * This sbcontext makes it clearer and faster to create and verify variable objects.
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
     */
    MUT,

    /**
     * A [VAR] subinstruction, used to declare a variable reference.
     * This subcontext makes it clearer and faster to search and retrieve variable objects,
     * either cloning, moving, or pointer acquisition.
     */
    REF,

    /**
     * Declare a procedure, which is then encapsulated into a ProcBox, which contains other information such as:
     *  - Source Location
     *  - Deserialized Signature -- The signature is in bytecode form, and is deserialized so that it's easier to interpret on demand.
     *  - Body
     */
    PROC,


    TYPE,

}