    li a7, 0x8000000
    addi x2, x0, 1
    addi x1, x0, 1
step:
    addi x2, x2, 1
    mul x1, x1, x2
    sw x1, 0(a7)
    j step
