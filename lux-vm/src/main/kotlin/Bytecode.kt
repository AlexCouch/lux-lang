@ExperimentalUnsignedTypes
enum class InstructionSet(val code: UByte){
    /**
     * Move the given data into the given address
     *
     * Operands:
     * Left operand is the destination, right operand is the target data.
     *
     * Example:
     *  MOVE    0x0005  0x0010 ;Move the data at memory address 16 into the address 5
     */
    MOVE(0xff.toUByte()),

    /**
     * Move the given byte data into the given address
     *
     * Operands:
     *  Left is the destination address, right is the byte data to be moved into the destination
     *
     * Example:
     *  MOVB    0x0005  0xa ;Move the integer 9 of size byte into memory address 5
     */
    MOVB(0xef.toUByte()),
    /**
     * Move the given word data into the given address
     *
     * Operands:
     *  Left is the destination address, right is the word data to be moved into the destination
     *
     * Example:
     *  MOVW    0x0005  0xa1 ;Move the integer 161 of size word into memory address 5
     */
    MOVW(0xee.toUByte()),
    /**
     * Move the given double word data into the given address
     *
     * Operands:
     *  Left is the destination address, right is the double word data to be moved into the destination
     *
     * Example:
     *  MOVD    0x0005  0x03a1 ;Move the integer 929 of size double word into memory address 5
     */
    MOVD(0xed.toUByte()),
    /**
     * Move the given long word data into the given address
     *
     * Operands:
     *  Left is the destination address, right is the long word data to be moved into the destination
     *
     * Example:
     *  MOVD    0x0005  0x03a19b ;Move the integer 237979 of size long word into memory address 5
     */
    MOVQ(0xec.toUByte()),

    /**
     * Move the given byte data into the given address
     *
     * Operands:
     *  Left is the destination address, right is the byte data to be moved into the destination
     *
     * Example:
     *  SMOVB    0x0005  0xa ;Move the integer 10 of size byte into memory address 5
     */
    SMOVB(0xeb.toUByte()),
    /**
     * Move the given word data into the given address
     *
     * Operands:
     *  Left is the destination address, right is the word data to be moved into the destination
     *
     * Example:
     *  SMOVW    0x05  0xff3a ;Move the integer -198 of size word into memory address 5
     */
    SMOVW(0xea.toUByte()),
    /**
     * Move the given double word data into the given address
     *
     * Operands:
     *  Left is the destination address, right is the double word data to be moved into the destination
     *
     * Example:
     *  MOVD    0x0005  0xffff03a1 ;Move the integer -64,607 of size double word into memory address 5
     */
    SMOVD(0xe9.toUByte()),
    /**
     * Move the given long word data into the given address
     *
     * Operands:
     *  Left is the destination address, right is the long word data to be moved into the destination
     *
     * Example:
     *  MOVD    0x0005  0xFFFFFFFF0003A19B ;Move the integer -4,294,729,317 of size long word into memory address 5
     */
    SMOVQ(0xe8.toUByte()),


    /**
     * Jump to the location as given at the top of the stack. This can be used in conjunction with a return address
     * at the end of a frame. When we are popping a frame, we are really popping everything creating in the frame.
     *
     * No operands
     */
    JMP(0xfe.toUByte()),
    /**
     * PUSH the data at the given address onto the stack as a copy. This will be popped at the end of the frame if we
     * pop the current frame, via pushing 0xff, we push the current instruction poiter onto the stack, which can then be
     * restored for later use when it gets popped, or used with JUMP
     *
     * Operands:
     *  Data type followed by data itself
     *
     */
    PUSH(0xfd.toUByte()),

    /**
     * POP the data at the top of the stack, which then gets deallocated.
     */
    POP(0xfc.toUByte()),

    /**
     * A pointer to the top of the stack. This is so that we can get whatever is on the top of the stack and use it for something
     * such as moving that data somewhere else or using it as a jump target
     */
    TOP(0xb0.toUByte()),

    /**
     * Takes the two given operands (left is first, right is second) and adds them together and stores on the top
     * of the stack
     *
     * Example:
     *  ADD     5,  3
     *
     * The top of the stack may either be the left or the right. This takes the value on the top of the stack as the operand.
     * Example:
     *  ADD     TOP, 3
     */
    ADD(0xa0.toUByte()),


    /**
     * Takes the two given operands (left is first, right is second) and subtracts the right from the left and stores on the top
     * of the stack
     *
     * Example:
     *  SUB     5,  3
     *
     * The top of the stack may either be the left or the right. This takes the value on the top of the stack as the operand.
     * Example:
     *  SUB     TOP, 3
     */
    SUB(0xa1.toUByte()),
    /**
     * Takes the two given operands (left is first, right is second) and multiples them together and stores on the top
     * of the stack
     *
     *
     * Example:
     *  MUL     5,  3
     *
     * The top of the stack may either be the left or the right. This takes the value on the top of the stack as the operand.
     * Example:
     *  MUL     TOP, 3
     */
    MUL(0xa2.toUByte()),
    /**
     * Takes the two given operands (left is first, right is second) and divides left from the right and stores on the top
     * of the stack
     */
    DIV(0xa3.toUByte()),

    /**
     * This does the same thing as ADD except it operates on signed values
     *
     * Example:
     *  SADD     0x05,  0xf3 ;Add -13 into memory address 0x05
     *
     */
    SADD(0xa4.toUByte()),

    /**
     * This does the same thing as SUB except it operates on signed values
     *
     * Example:
     *  SADD     0x05,  0xf3 ;Subtract -13 into memory address 0x05
     *
     */
    SSUB(0xa5.toUByte()),

    /**
     * This does the same thing as MUL except it operates on signed values
     *
     * Example:
     *  SMUL     0x05,  0xf3 ;Multiply -13 with/into memory address 0x05
     *
     */
    SMUL(0xa6.toUByte()),

    /**
     * This does the same thing as DIV except it operates on signed values
     *
     * Example:
     *  SDIV     0x05,  0xf3 ;Divide -13 by/into memory address 0x05
     *
     */
    SDIV(0xa7.toUByte()),

    /**
     * This is an operand modifier which allows us to specify that the current operand is a reference to some place
     * in memory. This takes an operand and that is the location in memory
     *
     * Example:
     *  ADD     REF 5,  10 ;Add 10 to whatever is stored in memory address 10 and push it onto the stack
     */
    REF(0xc0u),

    /**
     * The current pointer in the instruction. This can be used for saving stack frames
     * and returning back to a call site.
     */
    INSPTR(0xc1u),

    BYTE(0xc2u),
    WORD(0xc3u),
    DWORD(0xc4u),
    QWORD(0xc5u),
    SBYTE(0xc6u),
    SWORD(0xc7u),
    SDWORD(0xc8u),
    SQWORD(0xc9u),

    /**
     * Compares two operands and pushes a 1 onto the stack if they are the same, 0 otherwise
     */
    CMP(0xc6.toUByte()),

    /**
     * Compares if the left operand is less than or equal to the operand on the right, pushing 1 on the stack if so,
     * 0 otherwise
     */
    LE(0xc7.toUByte()),
    /**
     * Compares if the left operand is less than the operand on the right, pushing 1 on the stack if so,
     * 0 otherwise
     */
    LT(0xc8.toUByte()),
    /**
     * Compares if the left operand is greater than or equal to the operand on the right, pushing 1 on the stack if so,
     * 0 otherwise
     */
    GE(0xc9.toUByte()),
    /**
     * Compares if the left operand is greater than the operand on the right, pushing 1 on the stack if so,
     * 0 otherwise
     */
    GT(0xca.toUByte()),

    /**
     * Jump if the first operand is equal to the second operand, to the target given by the third operand.
     */
    JEQ(0xd1.toUByte()),
    /**
     * Jump if the first operand is less than the second operand, to the target given by the third operand.
     */
    JLT(0xd2.toUByte()),
    /**
     * Jump if the first operand is less than or equal to the second operand, to the target given by the third operand.
     */
    JLE(0xd3.toUByte()),
    /**
     * Jump if the first operand is greater than the second operand, to the target given by the third operand.
     */
    JGT(0xd4.toUByte()),
    /**
     * Jump if the first operand is greater than or equal to the second operand, to the target given by the third operand.
     */
    JGE(0xd5.toUByte()),

    /**
     * Takes a memory address as left operand, and does a bitwise AND on its value with the right operand, then
     * saving the result in the given address.
     */
    AND(0x90.toUByte()),
    /**
     * Takes a memory address as left operand, and does a bitwise OR on its value with the right operand, then
     * saving the result in the given address.
     */
    OR(0x91.toUByte()),
    /**
     * Takes a memory address as left operand, and does a bitwise XOR on its value with the right operand, then
     * saving the result in the given address.
     */
    XOR(0x92.toUByte()),
    /**
     * Takes a memory address as operand and does a bitwise inversion on its operators
     */
    INV(0x93.toUByte()),
    /**
     * Takes a memory address as left operand, and does a bitwise right shift on its value with the right operand, then
     * saving the result in the given address.
     */
    SHR(0x94.toUByte()),
    /**
     * Takes a memory address as left operand, and does a bitwise left shift on its value with the right operand, then
     * saving the result in the given address.
     */
    SHL(0x95.toUByte()),

    /**
     * This opcode is used as a variadic operand for telling the VM that there are no operands.
     * All opcodes without operands must have this operand.
     *
     * POP      => POP NOARGS
     */
    NOARGS(0x30u),

    /**
     * This opcode is used as a variadic operand for telling the VM that there are a certain number of operands.
     * This is used by opcodes with at least 1 operand.
     *
     *  MOV    0x05, 0x05 + 0x03 => MOV ARGS 4 0x05 0x05 OFFSET 0x03
     *
     *  POP 0x05       => POP ARGS 1 0x05
     *
     */
    ARGS(0x31u),

    /**
     * This opcode is used to do address offsetting in the positive direction. In lasm, this would look like
     *
     * some_label: .ascii 'Hello, world!'
     * mov 0x05, [some_label+5]             ;Move data at index 5 in ascii string 'some_label' into address 0x05, effectively moving character ',' into 0x05
     *
     * This turns into the bytecode
     *
     * MOV 0x05 0x00 OFFSET 0x05             ;The label some_label is at address 0 in the instruction, so the offset of some_label + 0x05 is just 0x05
     */
    OFFSET(0x32u),
    /**
     * This opcode is used to do address offsetting in the negative direction. In lasm, this would look like
     * ...                                  ;Some other code here, let's say 20 other instructions, making some_label at index 21
     * some_label: .ascii 'Hello, world!'
     * mov 0x05, [some_label-5]             ;Move data at address 5 from the start of some_label into address 0x05, effectively moving character ',' into 0x05
     *
     * This turns into the bytecode
     *
     * MOV 0x05 0x15 NOFFSET 0x05           ;The label some_label is at address 0 in the instruction, so the offset of some_label - 0x05 is just 0x10 aka 16
     */
    NOFFSET(0x33u),
}