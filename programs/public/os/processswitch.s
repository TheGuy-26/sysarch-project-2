# Address space distribution
# PID: 0x0 [0 -> 1, 1 -> 2]
# Registers for Expection handler: 0x4
# Fibonacci: 0x100
# Factorial: 0x200
# MEPC: Base address + 128

# set up exception handler
_start: 
    la t0, exception_handler        # setting up exception handler
    csrw mtvec, t0                  # ...

# set up mepc to point to the first instruction of the fibonacci function
    la t0, fibonacci
    csrw mepc, t0

# enable and set up interrupts as needed
    # enable MIE (bit 3 of mstatus) ?
    li t0, 0x80
    csrs mie, t0 # set the Bit 7 of MEI to 1
    jal ra, reset_time

# set up data structures for process control blocks
    # initializing the PCB for fibonacci at 0x100
    la t0, fibonacci
    li t1, 0x100
    sw t0, 0(t1)
    sw x0, 0(x0) # initialising PID
    li sp, 0x104 # initial stack pointer after fibonacci address
    # sw t0, 128(t1) # initialise MEPC for P1

    # initializing the PCB for factorial at 0x200
    la t0, factorial
    li t1, 0x200
    sw t0, 0(t1)
    sw t0, 128(t1) # initialise MEPC for P2

# execute the fibonacci function until you get an interrupt
    mret

exception_handler:
    # TODO: save some registers
    sw t0, 4(x0) # x5
    sw sp, 8(x0) # x2

    # setting up new timer interrupt + implement process switch
    # jal ra, reset_time 

    lw t0, 0(x0) # load value of PID at 0x0
    beq t0, x0 switch_to_process_b



# default
switch_to_process_a:
    li sp, 0x200 # base address for P2
    j store_pcb

switch_to_process_b:
    li sp, 0x100 # base address for P1
    # save registers
    # switch PID
    # reset time

store_pcb:
    # sp: base address of process
    sw x1, 4(sp)

    # loading saved value of sp
    lw t0, 8(x0)
    sw t0, 8(sp) # x2

    sw x3, 12(sp)
    sw x4, 16(sp)

    # loading saved value of t0
    lw t0, 4(x0)
    sw t0, 20(sp) # x5

    sw x6, 24(sp)
    sw x7, 28(sp)
    sw x8, 32(sp)
    sw x9, 36(sp)
    sw x10, 40(sp)
    sw x11, 44(sp)
    sw x12, 48(sp)
    sw x13, 52(sp)
    sw x14, 56(sp)
    sw x15, 60(sp)
    sw x16, 64(sp)
    sw x17, 68(sp)
    sw x18, 72(sp)
    sw x19, 76(sp)
    sw x20, 80(sp)
    sw x21, 84(sp)
    sw x22, 88(sp)
    sw x23, 92(sp)
    sw x24, 96(sp)
    sw x25, 100(sp)
    sw x26, 104(sp)
    sw x27, 108(sp)
    sw x28, 112(sp)
    sw x29, 116(sp)
    sw x30, 120(sp)
    sw x31, 124(sp)
    
save_mepc:
    csrr t0, mepc
    sw t0, 128(sp)

switch_pid_and_new_sp:
    lw t0, 0(x0)
    xori t0, t0, 1
    sw t0, 0(x0)

    # load sp of P1
    li sp, 0x100
    # check if pid = 1, switch to sp of P2 
    beq t0, x0, load_pcb
    xori sp, sp, 0x300 # mask that flips bit 8 and 9

load_pcb:
    # flip bit 7 of MIP (clearing Timer Interrupt Pending)
    li t0, 0x80
    csrc mip, t0

    # load mepc for next process
    lw t0, 128(sp)
    # addi t0, t0, 4
    csrw mepc, t0

    # restore registers
    lw x1, 4(sp)

    lw x3, 12(sp)
    lw x4, 16(sp)

    lw x7, 28(sp)
    lw x8, 32(sp)
    lw x9, 36(sp)
    lw x10, 40(sp)
    lw x11, 44(sp)
    lw x12, 48(sp)
    lw x13, 52(sp)
    lw x14, 56(sp)
    lw x15, 60(sp)
    lw x16, 64(sp)
    lw x17, 68(sp)
    lw x18, 72(sp)
    lw x19, 76(sp)
    lw x20, 80(sp)
    lw x21, 84(sp)
    lw x22, 88(sp)
    lw x23, 92(sp)
    lw x24, 96(sp)
    lw x25, 100(sp)
    lw x26, 104(sp)
    lw x27, 108(sp)
    lw x28, 112(sp)
    lw x29, 116(sp)
    lw x30, 120(sp)
    lw x31, 124(sp)

    jal ra, reset_time

    # restoring t0 and t1 in the end 
    lw x5, 20(sp) # t0
    lw x6, 24(sp) # t1

    lw x2, 8(sp)

# return to user mode to continue with next process
    mret

reset_time:
    # mtimecmp := 300
    li t0, -1
    la t1, mtimecmp
    sw t0, 0(t1)
    li t0, 0
    sw t0, 4(t1)
    li t0, 329
    sw t0, 0(t1)

    # mtime := 0
    li t0, -1
    la t1, mtime
    sw t0, 0(t1)
    li t0, 0
    sw t0, 4(t1)
    sw t0, 0(t1)
    
    jr ra