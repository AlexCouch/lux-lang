# TODO

## Version 0.0.2-prototype (Comparisons, Pointer Maths, Labels, Comments, Bitwise Opcodes)
* [x] Redo the arithmetic operators so that they modify the destination (first operand) given the source data (second operand)
* [x] Implement arithmetic operators into lasm
* [x] Implement comparison opcodes
    - [x] CMP ;Equality comparison
    - [x] GT ;Greater than
    - [x] GE ;Greater than or equal to
    - [x] LE ;Less than
    - [x] LT ;Less than or equal to
    - [x] JLE ;Jump if less than or equal to
    - [x] JLT ;Jump if less than
    - [x] JGE ;Jump if greater than or equal to
    - [x] JGT ;Jump if greater than
    - [x] JEQ  ;Jump if equal to
* [x] Rewrite parser so that it is more robust and easier to implement new things.
    - Make the parser simpler with a higher-order function for parsing certain kinds of instructions
* [x] Finish implementing double word and quad word operations
    - movd, movq
* [x] Implement labels
    - ```
        some_label:
            mov 0x10, [0x5]
            push [0x10]
      
        jump some_label
      ```
* [x] Implement comments
    - ```
        mov 0x10, [0x5] ;Move whatever is in the address stored in 0x5 to 0x10
      ```
* [x] Finish implementing bitwise opcodes and verify their operations are accurate and correct
    - Make sure that the bitwise operations between different sized data makes sense and works as it should
    
* [ ] Change the data to be unsigned bytes, and implement signed version of instructions:
    - [x] Change default instructions to operate on unsigned bytes
    - [ ] movsb : move signed bytes
    - [ ] movsw : move signed word
    - [ ] movsd : move signed double word
    - [ ] movsq : mov signed quad word
    - [ ] addsb : add signed byte (right) to given address (left)
    - [ ] subsb : sub signed byte (right) to given address (left)
    - [ ] mulsb : multiply signed byte (right) to given address (left)
    - [ ] divsb : divide signed byte (right) to given address (left)
    
    - This will ensure that any time that signed data is never specified, then we will always have the unsigned data available for use for unsigned operations

* [x] Add overflow logic to data arithmetics so that if a byte overflows then we reset that byte and flow into the next byte (words, dwords, qwords)

## Version 0.0.3-prototype (FFI)
* [ ] Implement sections
    - ```
        ;This is the start of the code section where the code section where all the executable code goes
        @code
            mov 0x10, 0x5
        
      ```
* [ ] Implement code and data sections
    - ```
        ;This is the start of the code section where the code section where all the executable code goes
        @code
            mov 0x10, 0x5
      
        ;This is the start of the data section where all the reusable data are there during assembly but not here during execution.
        ;This section is for referencing global data such as strings or other kinds of constants
        @data
            my_str: .ascii 'Hello, world!'
      ```
* [ ] Implement platform sections
    - This is so that certain kinds of data and code exist for a certain platform.
    - This is also good for directives such as `.load`, `.include`, `.using`
    - ```
        @windows
            .load 'some_lib_win.dll'
        @macos
            .load 'some_lib_mac.dyn'
        @linux
            .load 'some_lib_linux.so'
      ```
* [ ] Implement directive for including other lasm files during assembly
    - ```
        .include 'some_lib.lasm'
      ```
* [ ] Implement directive for loading up a native library (.so, .a, .dll, .dyn) to be used as FFI
    - This will require a way for the virtual machine to dynamically load up the foreign library via JNA at runtime
    - ```
        .load 'some_lib.a'
      ```
* [ ] Implement directive for using a symbol in some included library.
    - ```
        .load 'some_lib.a'
        .using some_proc
        
        push insptr     ;Push the current instruction pointer location to the stack
        jmp some_proc   ;Jump to the procedure and start executing it
      ```