    li a7, 0x4000000
    addi x1, x0, 0
    addi x2, x0, 1
loop: 
    add x3, x2, x1
    add x1, x2, x0
    add x2, x3, x0
    sw x3, 0(a7)
    j loop
