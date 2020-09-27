# Lux-lang
Lux is a python like programming language designed to be a modernized, cleaned up python with static typing, pattern matching, algebraic data types, abstract classes, proper inheritance, generics, and abstract expression (aka pure functions).

## TODO
Legend: x = completed; - in progress<br>
* [x] Variables
    * [x] Stack allocated variables (aka legacy variables)
    * [x] Heap allocated variables
        * [x] Const
        * [x] Var
* [x] Procedures (otherwise known as functions)
* [x] Static Type Checking
* [ ] Control Flow
* [ ] Pattern matching
* [ ] Algebraic Data Types
* [ ] Classes
* [ ] Inheritance
* [ ] Abstract Classes
* [ ] Code blocks
    - These are reusable blocks of code which are also expressions. 
        These may have input and/or output(called procedural blocks) and they may be passed as arguments or assigned to variables
* [ ] Abstract Expressions (Functions)
    - These are purely mathematical/logically derived functions based on abstractions in lambda calculus. These provide a way to abstract over any expression into a single input variable and then applied later on. When applied to an expression, the compiler will reduce it down to something simpler. This is to make mathematical operations and equations simpler during compiletime so that the VM has to do less for the same thing done in other languages.
* [ ] Associated procedures and variables
    - This gives someone the ability to associate heap/reference counted variables with a class, or even a procedure. This is equivalent to class functions or class variables in python.
* [ ] FFI (Foreign Function Interfacing)