# Lux VM
The Lux Virtual Machine is a minimal, lightweight, *single responsibility instruction* VM modeled after physical machines to enable programming language developers to use a *Write once, Run Everywhere* backend that takes only once to link, and is easily portable and immediately usable by anyone.

Lux VM also enables a user to program the VM via a custom assembly language using an assembler, and running in the VM directly. The allows for a complete separation from the Lux Lang and the Lux VM, and can act as a separate programming environment, similar to a physical CPU.

## Instructions
> Note: Not all instructions have been documented due to time constraints (had plans to go to at the time of this writing).

> Note: Not all syntax in the following examples are implemented but the bytecode themselves are implemented and can be directly written to via intArrayOf() and passing it into a new VM instance
* MOV
    - Moves given data from one address to another address.
    - Two operands: target (second operand), dest (first operand)
    - Example
        ```asm
        MOV     0x10, 0x5 ;Move data at address 5 to address 16
        ```
* MOVB
    - Moves data of size byte into the given address. This is the same as directly writing data of size byte into that memory address.
    - This also accepts a reference to some address in the executable, to load data of size byte, via some label (see example).
    - This also accepts a reference to some place on the stack or in memory. This allows for specifying moving data of size byte.
    - Examples:
        ```
        MOVB    0x10, 0x8 ;Move the byte sized data 0x8 (8 in decimal) into memory address 16
        
        ;Note!! This is not yet implemented but will be soon!!!
        some_data: .byte    8
        MOVB    0x10, [some_data] ;Move the byte sized data at the address labeled 'some_data' into the address 16
        ```

* MOVW
    - Moves data of size word into the given address. This is the same as directly writing data of size word into that memory address.
    - This also accepts a reference to some address in the executable, to load data of size word, via some label (see example).
    - This also accepts a reference to some place on the stack or in memory. This allows for specifying moving data of size word.
    - Examples:
        ```
        MOVW    0x10, 0x80 ;Move the byte sized data 0x80 (128 in decimal) into memory address 16
        
        ;Note!! This is not yet implemented but will be soon!!!
        some_data: .word    128
        MOVB    0x10, [some_data] ;Move the word sized data at the address labeled 'some_data' into the address 16
        ```

* MOVD
    - Moves data of size double word into the given address. This is the same as directly writing data of size double word into that memory address.
    - This also accepts a reference to some address in the executable, to load data of size double word, via some label (see example).
    - This also accepts a reference to some place on the stack or in memory. This allows for specifying moving data of size double word.
    - Examples:
        ```
        MOVD    0x10, 0x80302 ;Move the byte sized data 0x80 (525,058 in decimal) into memory address 16
        
        ;Note!! This is not yet implemented but will be soon!!!
        some_data: .dword    0x80302
        MOVB    0x10, [some_data] ;Move the double word sized data at the address labeled 'some_data' into the address 16
        ```
* MOVL
    - Moves data of size long word into the given address. This is the same as directly writing data of size long word into that memory address.
    - This also accepts a reference to some address in the executable, to load data of size long word, via some label (see example).
    - This also accepts a reference to some place on the stack or in memory. This allows for specifying moving data of size long word.
    - Examples:
        ```
        MOVD    0x10, 0x80302 ;Move the long word sized data 0x80 (525,058 in decimal) into memory address 16
        
        ;Note!! This is not yet implemented but will be soon!!!
        some_data: .lword    0x80302
        MOVB    0x10, [some_data] ;Move the long word sized data at the address labeled 'some_data' into the address 16
        ```
      
* TOP
    - A reference to the top of the stack. This is meant to be used for copying something on the top of the stack and moving it somewhere else.
    - Example
        ```
        MOV     0x10, TOP ;Copy and move what's on the top of the stack into memory address 16
        ```
* PUSH
    - Pushes data onto the top of the stack. This also allows taking data at a given address and pushing it onto the stack.
    - Example:
        ```
        PUSH    0x53        ;Push the byte 0x53 (83 in decimal) onto the stack
  
        MOV     0x10, TOP   ;Move the data on the top of the stack (in our case, 0x53) and move it into address 16
        POP                 ;Pop whatever is on the stack (see POP)
        PUSH    [0x10]      ;Push whatever data is at address 16 onto the top of the stack
        ``` 
* POP
    - Pops the top of the stack, effectively zeroing it out and decrementing the stack pointer.
    - Example:
        ```
        PUSH    0x35    ;Push 0x35 onto the stack
        POP             ;Pop 0x35 off the stack
        ```
      
* REF
    - Takes the data at a given address and pulls that data into the operand. This is effectively a dereference of a given pointer.
    - In lasm syntax, this is denoted by placing square brackets around a label or a hex code.
    - In bytecode, this is translated to a REF opcode which is used to modify a given operand to dereference an address.
    - Example:
        ```
        some_label: .byte 0x35
        MOV     0x10, 0x35      ;Move 0x35 into the memory address 16
        PUSH    [0x10]          ;Push the data at address 16 (0x35) onto the stack
        POP                     ;Pop 0x35 off the stack
        PUSH    [some_label]    ;Push the data at the address labeled some_label onto the stack (0x35)
        POP                     ;Pop 0x35 off the stack
        ```
* JMP
    - Jumps to the given location in the executable instructions.
    - This can either be a label, an address, or data at some location either on the stack or in memory.
    - Example:
        ```
        MOV     0x10, 0x2           ;Move 2 into 0x10, which is then used later on to jump to index 2
        JMP     3                   ;Jump to instruction index 3
        MOV     0x10, 0x0           ;Move 0 into address 0x10
        PUSH    [0x10]              ;Push data at address 0x10 to the stack
        JMP     [0x10]              ;Jump to index 2, which is stored in memory address 0x10 
        ```
* LT
    - Takes two operands, and checks if the left is less than the right
    - Pushes a 1 if true, 0 otherwise, onto the stack
    - Example:
        ```
        MOVB    5, 5    ;Write byte literal 5 into memory address 5 
        ADD     5, 10   ;Add 10 to memory address 5
        LT      [5], 10 ;This will push a 0 onto the stack since 15 is in memory address 5, and we are checking if data in memory address 5 is 15, and 15 > 10
        ```
* LE
    - Takes two operands, and checks if the left is less than or equal to the right
    - Pushes a 1 if true, 0 otherwise, onto the stack
    - Example:
        ```
        MOVB    5, 5    ;Write byte literal 5 into memory address 5 
        ADD     5, 10   ;Add 10 to memory address 5
        LE      [5], 10 ;This will push a 0 onto the stack since 15 is in memory address 5, and we are checking if data in memory address 5 is 15, and 15 > 10
        ```
* GT
    - Takes two operands, and checks if the left is gerater than the right
    - Pushes a 1 if true, 0 otherwise, onto the stack
    - Example:
        ```
        MOVB    5, 5    ;Write byte literal 5 into memory address 5 
        ADD     5, 10   ;Add 10 to memory address 5
        GT      [5], 10 ;This will push a 1 onto the stack since 15 is in memory address 5, and we are checking if data in memory address 5 is 15, and 15 > 10
        ```
* GE
    - Takes two operands, and checks if the left is greater than or equal to the right
    - Pushes a 1 if true, 0 otherwise, onto the stack
    - Example:
        ```
        MOVB    5, 5    ;Write byte literal 5 into memory address 5 
        ADD     5, 10   ;Add 10 to memory address 5
        LT      [5], 10 ;This will push a 1 onto the stack since 15 is in memory address 5, and we are checking if data in memory address 5 is 15, and 15 > 10
        ```