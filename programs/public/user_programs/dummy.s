# Dummy:
# 1. Run for n cycles, where n is given in register a1
# 2. exit

    srli a1, a1, 1   # divide n by 2
    addi a1, a1, -4  # subtract 4 from n to account for 2 instructions at the start and 2 at the end
loop:
    addi a1, a1, -1 # decrement n
    bnez a1, loop   # if n is not zero, repeat
    li a7, 93       # syscall number for exit
    ecall           # exit the process