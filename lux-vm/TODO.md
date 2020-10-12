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
    
* [x] Change the data to be unsigned bytes, and implement signed version of instructions:
    - [x] Change default instructions to operate on unsigned bytes
    - [x] sadd : add signed byte (right) to given address (left)
    - [x] ssub : sub signed byte (right) to given address (left)
    - [x] smul : multiply signed byte (right) to given address (left)
    - [x] sdiv : divide signed byte (right) to given address (left)
    
    - This will ensure that any time that signed data is never specified, then we will always have the unsigned data available for use for unsigned operations

* [x] Add overflow logic to data arithmetics so that if a byte overflows then we reset that byte and flow into the next byte (words, dwords, qwords)
* [ ] Give `pop` an operand for location to pop off the stack to

* [ ] Add variadic operands for certain operators, by implementing operand specifiers
    - [ ] No operands
    - [ ] ["num_operands"] [num] : If we specify "num_operands" specifier, then it must be followed up with a value
* [ ] Add offsetting logic
    - Example:
      ```
        mov     0x05 + 0x05, [0x0a + 0x05] ;Mov whatever is in the address pointed to in 0x0a + 0x05 (0x0f) into 0x05 + 0x05 (0x0a)
        mov     0x15 - 0x04, 0x35          ;Mov 0x35 into 0x15 - 0x04 (0x11)
        mov     0x05 * 3 - 4, 0x35         ;Mov 0x35 into 0x05 * 3 (0x0f) - 4 (0x0b)
      ```

## Version 0.0.3-prototype (Directives, Memory layout changes, Sections)
* [ ] Correct memory layout so that the executable, stack, and memory are all part of the same contiguous region of memory.
    - This is necessary for data sections (data, rodata, bss) to coexist with the memory, stack, and instructions
    - The below directives can be implemented correctly
* [ ] Add a way to create new directives
* [ ] Add directives such as
    - [ ] `.string` for creating a string without null termination
    - [ ] `.ascii` for creating a string with null termination
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
    
## Version 0.0.4-prototype (Linker, Debugger)
* [ ] Add a directive for including another lasm file
    - `.include [string]`
* [ ] Add a new output type of *lib*, with extension *.llib
* [ ] Create linker so that llib's can be linked into single *.lexe file
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
* [ ] Add a way to enable different contexts for the VM to run in, one for normal mode, and one for debug mode
* [ ] Add an interactive debugger with an environment with commands to
    - [ ] Add breakpoints
    - [ ] Show memory address data or ranges of address data
    - [ ] Show stack
    - [ ] Show current/next instruction
        - [ ] This should be customizable so that you can set any range of instructions
        - [ ] This should have color coding depending on the syntax
* [ ] Cache the comments so that they can be reviewed in the debugger

## Version 0.0.5-prototype (FFI)
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