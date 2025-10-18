# Fixed registers
# t6: offset
# t5: cursor color
# t4: cell color

paint_start:
    # setting the default position of the cursor (setting the color at the given position)
    li t6, 1980
    li t5, 0xffffff # initial cursor color (white)
    li t4, 0 # initial cell color (black)
    j render_cursor

# wait for keyboard input
wait_for_keyboard:
    la t0, keyboard_ready
    lb t1, 0(t0)
    beq t1, x0, wait_for_keyboard

# read keyboard input
    la t0, keyboard_data
    lb t1, 0(t0)

    # case distinction
    li t0, 0x77
    beq t1, t0, move_up

    li t0, 0x73
    beq t1, t0, move_down

    li t0, 0x61
    beq t1, t0, move_left

    li t0, 0x64
    beq t1, t0, move_right

    li t0, 0x20
    beq t1, t0, drop_color

    li t0, 0x72
    beq t1, t0, flip_red

    li t0, 0x67
    beq t1, t0, flip_green

    li t0, 0x62
    beq t1, t0, flip_blue
    
    li t0, 0x62
    beq t1, t0, flip_blue

    j wait_for_keyboard

flip_red:
    li t0, 0xff0000
    xor t5, t5, t0
    j render_cursor


flip_green:
    li t0, 0x00FF00
    xor t5, t5, t0
    j render_cursor


flip_blue:
    li t0, 0x0000FF
    xor t5, t5, t0
    j render_cursor


drop_color:
    # copying cursor color to cell cursor
    or t4, t5, x0
    j wait_for_keyboard

move_up:
    li t3, 124
    blt t6, t3, wait_for_keyboard

    jal ra, clear_cursor
    addi t6, t6, -128
    j move_cursor


move_down:
    li t3, 3968
    bge t6, t3, wait_for_keyboard

    jal ra, clear_cursor
    addi t6, t6, 128
    j move_cursor


move_left:
    # doing nothing if we are on the edge (living on the edge!!!!)
    andi t3, t6, 127
    beq t3, x0, wait_for_keyboard

    jal ra, clear_cursor
    addi t6, t6, -4
    j move_cursor


move_right:
    addi t3, t6, 4
    andi t3, t3, 127
    beq t3, x0, wait_for_keyboard
    
    jal ra, clear_cursor
    addi t6, t6, 4
    j move_cursor

render_cursor:
    la t0, display

    # adding offset to the base address of the display
    add t0, t0, t6

    sw t5, 0(t0)
    j wait_for_keyboard

move_cursor:
    la t0, display

    # adding offset to the base address of the display
    add t0, t0, t6

    # reading color of target cell into t4
    lw t4, 0(t0)

    sw t5, 0(t0)
    j wait_for_keyboard

# to erase cursor color and restore cell color
clear_cursor:
    la t0, display

    # adding offset to the base address of the display
    add t0, t0, t6

    sw t4, 0(t0)
    jr ra
