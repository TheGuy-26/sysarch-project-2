# Address space distribution
# PID: 132(0) [0 -> 0 (startup), 1 -> 1, 2 -> 2, 3 -> 3, 4 -> 4, 5 -> 5, 6 -> 6, 7 -> 7, 8 -> 8]
# proc_count: 136(0)
# value of ra: 140(0)
# previous job time: 144(0)
# program address: 0(base_address)
# estimated time: 4(base_address)
# startup: 0x000
# p1: 0x100
# p2: 0x200
# p3: 0x300
# p4: 0x400
# p5: 0x500
# p6: 0x600
# p7: 0x700
# p8: 0x800

# intermediary registers: 152:188
# process-valid bitstring: 188 (0b 0 0000 0000)
# new PCB base upon creation: 192

_start:
    # Set up exception handler
    la t0, exception_handler        # setting up exception handler
    csrw mtvec, t0                  # ...

# set up mepc to point to the first instruction of the startup function
    la t0, startup
    csrw mepc, t0
    
    la t0, startup
    sw t0, 0(x0)        # instr_addr/mepc

    sw x0, 132(x0)      # initialising global PID holder 
    sw x0, 136(x0)      # initialising process counter
    sw x0, 188(x0)      # initialising process-valid bitstring
    
    mret

# Shutdown if all processes terminate
shutdown:
    j shutdown

# -------------------------------
exception_handler:
    # store registers used by exception handler to memory
    sw t0, 152(x0)
    sw t1, 156(x0)
    sw t2, 160(x0)
    sw t3, 164(x0)
    sw t4, 168(x0)
    sw t5, 172(x0)
    sw a0, 176(x0)
    sw a1, 180(x0)
    sw ra, 184(x0)

    # load current mtime
    la t0, mtime
    lw t0, 0(t0)

    addi t0, t0, -86
    sw t0, 144(x0)

    li t1, 93  # exit
    beq a7, t1, kill_process

    # Get syscall number
    li t1, 221 # exec
    beq a7, t1, create_new_process

    # Default: update PCB for current process and schedule next job
    # jal ra, advance_mepc
    # TODO: save context
    jal ra, get_current_pcb
    jal ra, save_context
    j schedule_next_process

# -----------------------------
create_new_process: 
    # Load current process count from 136
    lw t0, 136(x0)
    li t1, 8
    bge t0, t1, limit_reached

    # update process count
    addi t0, t0, 1
    sw t0, 136(x0)

    # set process-valid bit at 188
    jal ra, get_next_available_pid      # result in t0
    jal ra, set_pid_valid               # input in t0

    # Calculate new PCB address: PCB = PID * 0x100
    li t3, 0x100
    mul t1, t0, t3          # next available PID in t0

    sw a0, 0(t1)       # instruction address/mepc
    sw a1, 4(t1)       # estimated time @ 0x4 offset
    sw t1, 192(x0)     # storing new PCB base in memory

    jal ra, get_current_pcb     # a0 = base address of the process

    # Save context of current process
    jal ra, save_context    # input in a0

    # copy PCB to child job
    lw t1, 192(x0)     # restoring new PCB base from memory
    jal ra, copy_registers_to_new_pcb   # input in t1 and a0

    # if syscall was from startup -> return
    beq a0, x0, return_to_startup

#    jal ra, advance_mepc
    j schedule_next_process

return_to_startup:
    # restore registers used by exception handler to memory
    jal ra, advance_mepc

    lw t0, 152(x0)
    lw t2, 160(x0)
    lw t1, 156(x0)
    lw t3, 164(x0)
    lw t4, 168(x0)
    lw t5, 172(x0)
    lw a0, 176(x0)
    lw a1, 180(x0)
    lw ra, 184(x0)

    mret

limit_reached:
    # return -1 in a0 to the calling process (update PCB)
    # save context?
    li a0, -1
    sw a0, 176(x0)
#    jal ra, advance_mepc
    jal ra, get_current_pcb
    jal ra, save_context
    j schedule_next_process

# --------------------------
kill_process:
    # if syscall was from startup dont decrement and just schedule next
    jal ra, get_current_pcb
    beq a0, x0, schedule_next_process

    # Get current process count from memory 136
    lw t0, 136(x0)
    addi t0, t0, -1    # Decrement process count
    sw t0, 136(x0)
    beqz t0, shutdown


    # deallocate PCB
    srli t0, a0, 8      # 0x100 -> 0x1
    jal ra, set_pid_invalid

    j schedule_next_process

# ------------------------------
save_context:
    # input: a0 base address of process

    sw ra, 140(x0) # save ra at 140
    jal ra, save_registers_to_pcb  
    lw ra, 140(x0)  # Restore ra
    csrr t0, mepc
    addi t0, t0, 4
    sw t0, 0(a0)                # store mepc + 4 to PCB

    # update estimated time in PCB
    lw t0, 144(x0)              # previous job time
    lw t1, 4(a0)
    sub t1, t1, t0
    sw t1, 4(a0)

    ret


advance_mepc:
    csrr t0, mepc
    addi t0, t0, 4
    csrw mepc, t0
    ret

# ------------------------
schedule_next_process:
    li t1, 0               # best PID = -1
    li t2, 0x7fffffff      # min time
    li t3, 1               # current iteration PID
next_proc_loop:
    # not valid pid -> skip
    li t0, 1
    sll t0, t0, t3
    lw t4, 188(x0)
    and t0, t4, t0
    beqz t0, skip

    li t4, 0x100
    mul t5, t3, t4
    addi a1, t5, 0         # PCB address

    lw t0, 4(a1)         # load est_time 
    blt t0, t2, update_best
skip:
    addi t3, t3, 1
    li t4, 9
    blt t3, t4, next_proc_loop
    j load_next

update_best:
    mv t2, t0
    mv t1, t3
    j skip

load_next:
    sw t1, 132(x0)  # update current PID
    li t4, 0x100
    mul t5, t1, t4
    add a1, x0, t5

    lw t0, 0(a1)
#    addi t0, t0, 4
    csrw mepc, t0

    jal ra, reset_time
    j load_registers_from_pcb

# ---------------------------
get_current_pcb:
    lw t0, 132(x0)
    li t1, 0x100
    mul t2, t0, t1
    addi a0, t2, 0        # returns address in a0
    ret

# -------------------------------
save_registers_to_pcb:
    # a0 = PCB base
    # before function call:
    # 0(a0) = instruction address
    # 4(a0) = estimated time
    # restoring registers used by exception handler from memory
    lw t0, 152(x0)
    sw t0, 24(a0)

    lw t1, 156(x0)
    lw t2, 160(x0)
    lw t3, 164(x0)
    lw t4, 168(x0)
    lw t5, 172(x0)
    lw a1, 180(x0)

    # restoring ra
    lw t0, 184(x0)
    sw t0, 8(a0)

    sw sp, 12(a0)
    sw gp, 16(a0)
    sw tp, 20(a0)
    sw t1, 28(a0)
    sw t2, 32(a0)
    sw s0, 36(a0)
    sw s1, 40(a0)
    sw a1, 48(a0)
    sw a2, 52(a0)
    sw a3, 56(a0)
    sw a4, 60(a0)
    sw a5, 64(a0)
    sw a6, 68(a0)
    sw a7, 72(a0)
    sw s2, 76(a0)
    sw s3, 80(a0)
    sw s4, 84(a0)
    sw s5, 88(a0)
    sw s6, 92(a0)
    sw s7, 96(a0)
    sw s8, 100(a0)
    sw s9, 104(a0)
    sw s10, 108(a0)
    sw s11, 112(a0)
    sw t3, 116(a0)
    sw t4, 120(a0)
    sw t5, 124(a0)
    sw t6, 128(a0)

    # restoring a0
    lw t0, 176(x0)
    sw t0, 44(a0)

    ret

load_registers_from_pcb:

    # a1 = PCB base
    lw ra, 8(a1)
    lw sp, 12(a1)
    lw gp, 16(a1)
    lw tp, 20(a1)
    lw t0, 24(a1)
    lw t1, 28(a1)
    lw t2, 32(a1)
    lw s0, 36(a1)
    lw s1, 40(a1)
    lw a0, 44(a1)
    lw a2, 52(a1)
    lw a3, 56(a1)
    lw a4, 60(a1)
    lw a5, 64(a1)
    lw a6, 68(a1)
    lw a7, 72(a1)
    lw s2, 76(a1)
    lw s3, 80(a1)
    lw s4, 84(a1)
    lw s5, 88(a1)
    lw s6, 92(a1)
    lw s7, 96(a1)
    lw s8, 100(a1)
    lw s9, 104(a1)
    lw s10, 108(a1)
    lw s11, 112(a1)
    lw t3, 116(a1)
    lw t4, 120(a1)
    lw t5, 124(a1)
    lw t6, 128(a1)

    lw a1, 48(a1)

    mret

get_next_available_pid:
    # result in t0
    lw t1, 188(x0)         # process vaild bitstring

    li     t0, 1           # index
    next_available_pid_loop:
    srli   t1, t1, 1
    andi   t2, t1, 1       # test LSb
    beqz   t2, got_zero
    addi   t0, t0, 1
    bnez   t1, next_available_pid_loop
    
    got_zero:
    # t0 already holds the index
    ret

set_pid_valid:
    # input in t0
    addi t2, t0, 0 # bitstring starts at index 1
    li t1, 1
    sll t1, t1, t2  # t1 <- 1 << t0
    lw t2, 188(x0)  # process-valid bitstring
    or t2, t2, t1   # setting the input bit in t0
    sw t2, 188(x0)
    # t0 is preserved
    ret

set_pid_invalid:
    # input in t0
    addi t2, t0, 0 # bitstring starts at index 1
    li t1, 1
    sll t1, t1, t2  # t1 <- 1 << t0
    not t1, t1

    lw t2, 188(x0)  # process-valid bitstring
    and t2, t2, t1   # setting the input bit in t0
    sw t2, 188(x0)
    # t0 is preserved
    ret


reset_time:
    # mtime := 0
    li t0, -1
    la t1, mtime
    sw t0, 0(t1)
    li t0, 0
    sw t0, 4(t1)
    sw t0, 0(t1)
    
    ret

copy_registers_to_new_pcb:
    # input: new base address in t1 and old in a0
    
    lw t0, 8(a0)
    sw t0, 8(t1)
    
    lw t0, 12(a0)
    sw t0, 12(t1)
    
    lw t0, 16(a0)
    sw t0, 16(t1)
    
    lw t0, 20(a0)
    sw t0, 20(t1)
    
    lw t0, 24(a0)
    sw t0, 24(t1)
    
    lw t0, 28(a0)
    sw t0, 28(t1)
    
    lw t0, 32(a0)
    sw t0, 32(t1)
    
    lw t0, 36(a0)
    sw t0, 36(t1)
    
    lw t0, 40(a0)
    sw t0, 40(t1)
    
    lw t0, 44(a0)
    sw t0, 44(t1)
    
    lw t0, 48(a0)
    sw t0, 48(t1)
    
    lw t0, 52(a0)
    sw t0, 52(t1)
    
    lw t0, 56(a0)
    sw t0, 56(t1)
    
    lw t0, 60(a0)
    sw t0, 60(t1)
    
    lw t0, 64(a0)
    sw t0, 64(t1)
    
    lw t0, 68(a0)
    sw t0, 68(t1)
    
    lw t0, 72(a0)
    sw t0, 72(t1)
    
    lw t0, 76(a0)
    sw t0, 76(t1)
    
    lw t0, 80(a0)
    sw t0, 80(t1)
    
    lw t0, 84(a0)
    sw t0, 84(t1)
    
    lw t0, 88(a0)
    sw t0, 88(t1)
    
    lw t0, 92(a0)
    sw t0, 92(t1)
    
    lw t0, 96(a0)
    sw t0, 96(t1)
    
    lw t0, 100(a0)
    sw t0, 100(t1)
    
    lw t0, 104(a0)
    sw t0, 104(t1)
    
    lw t0, 108(a0)
    sw t0, 108(t1)
    
    lw t0, 112(a0)
    sw t0, 112(t1)
    
    lw t0, 116(a0)
    sw t0, 116(t1)
    
    lw t0, 120(a0)
    sw t0, 120(t1)
    
    lw t0, 124(a0)
    sw t0, 124(t1)
    
    lw t0, 128(a0)
    sw t0, 128(t1)

    ret