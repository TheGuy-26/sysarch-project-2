start:
	jal ra, print_fixed_string
	jal ra, print_character
	jal ra, print_character
	jal ra, print_character
	jal ra, print_space
	jal ra, print_string
	jal ra, print_character
	jal ra, print_space
	jal ra, print_string
	jal ra, print_character
	j start

li	a0, 66 	# 
li 	a7, 4
loop:	ecall
	li	a0, 67 # C
	j	loop

print_character:
	li  a0, 0x21	# 0x21 -> "!"
	li	a7, 11		# print_character
	ecall
	ret

print_space:
	li  a0, 0x20	# 0x20 -> " "
	li	a7, 11		# print_character
	ecall
	ret


print_string:
	li a0, 0x4000 # start address of string
	li t0, 0x48   # 0x48-> "H"
	sb t0, 0(a0)
	li t0, 0x65   # 0x65 -> "e"
	sb t0, 1(a0)
	li t0, 0x6c   # 0x6c -> "l"
	sb t0, 2(a0)
	sb t0, 3(a0)
	li t0, 0x6f   # 0x6f -> "o"
	sb t0, 4(a0)
	sb x0, 5(a0)  # null terminator
	li a7, 4
	ecall
	ret

print_fixed_string:
	li a0, 0xf00
	li a7, 4
	ecall
	ret