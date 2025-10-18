# bootup
_start: 
    la t0, exception_handler        # setting up exception handler
    csrw mtvec, t0                  # ...

    # set mepc to user_systemcalls WHY NOT HARDWARE??
    la t0, user_systemcalls # user_systemcalls.s
    csrw mepc, t0
    mret                            # return to user mode

exception_handler:
    # save registers you need to handle the exception
    sw t0, 0(x0)
    sw t1, 4(x0)
    sw t2, 8(x0)
    sw t3, 12(x0)

    # check the cause of the exception
    csrr t0, mcause
    li t1, 8
    bne t0, t1, return # if not syscall

    # handle the system call
    # case 11
    li t1, 11
    beq a7, t1, print_char
    # case 4
    li t1, 4
    beq a7, t1, print_string
    # else
    j return

print_char:
    la t1, terminal_ready
    lw t0, 0(t1)
    andi t0, t0, 1 # extract LSB
    beq t0, x0, print_char
    andi t1, a0, 0xFF
    la t0, terminal_data
    sw t1, 0(t0)
    j return

print_string:
    add t2, x0, a0
    # t2 -> address pointer
    # t3 -> the char value

print_string_loop:
    # waiting for terminal
    la t1, terminal_ready
    lw t0, 0(t1)
    andi t0, t0, 1 # extract LSB
    beq t0, x0, print_string_loop

    # terminal ready
    lb t3, 0(t2)
    beq t3, x0, return # null-termination

    # printing one char
    la t0, terminal_data
    sb t3, 0(t0)

    # increment address
    addi t2, t2, 1

    j print_string_loop

return:
    csrr t0, mepc
    addi t0, t0, 4
    csrw mepc, t0
    # restore registers you saved and return to user mode
    lw t0, 0(x0)
    lw t1, 4(x0)
    lw t2, 8(x0)
    lw t3, 12(x0)

    mret
