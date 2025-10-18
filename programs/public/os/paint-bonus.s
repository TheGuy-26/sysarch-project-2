# Fixed registers
# t6: offset
# t5: cursor color
# t4: cell color
# s10: address of next char
# s11: input source [wait_for_keyboard/read_next_char]

paint_start:
    # setting the default position of the cursor (setting the color at the given position)
    li t6, 1980               # initial cursor position (15,15)
    li t5, 0xffffff           # initial cursor color (white)
    li t4, 0                  # initial cell color (black)
    la s11, wait_for_keyboard # initial address for input source
    j render_cursor

read_next_input:
    jr s11

# wait for keyboard input
wait_for_keyboard:
    la t0, keyboard_ready
    lb t1, 0(t0)
    beq t1, x0, wait_for_keyboard

# read keyboard input
    la t0, keyboard_data
    lb t1, 0(t0)

match_input:
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

    # BONUS PARTS
    li t0, 0x70
    beq t1, t0, pipette

    li t0, 0x6d
    beq t1, t0, mix

    li t0, 0x31 
    beq t1, t0, start_string_input

    j read_next_input

start_string_input:
    la s11, read_next_char      # set input source
    li s10, 0                   # initial index for next char

read_next_char:
    addi t0, x0, 1
    slli t0, t0, 16             # t0 <- 0x10000 (base address for string input)

    add t0, t0, s10
    addi s10, s10, 1            # increment offset s10 by 1 byte

    lb t1, 0(t0)
    beq t1, x0, end_string_input

    j match_input               # matching input in t1

end_string_input:
    la s11, wait_for_keyboard   # reset input source to keyboard
    j read_next_input

pipette:
    la t0, display

    # adding offset to the base address of the display
    add t0, t0, t6

    # updating cursor pixel
    sw t4 0(t0)

    # reading color of target cell into t5 (cursor color)
    add t5, t4, x0
    
    j render_cursor

mix:
    # New color components:
    # Red: t1
    # Green: t2
    # Blue: t3

    # load blue components of cell color and cursor color
    andi t0, t4, 0xff
    andi t1, t5, 0xff
    # calculate average
    add t3, t0, t1
    srai t3, t3, 1

    # load green components of cell color and cursor color
    srai t0, t4, 8
    andi t0, t0, 0xff
    
    srai t1, t5, 8
    andi t1, t1, 0xff
    # calculate average
    add t2, t0, t1
    srai t2, t2, 1

    # load red components of cell color and cursor color
    srai t0, t4, 16
    andi t0, t0, 0xff

    srai t1, t5, 16
    andi t1, t1, 0xff
    # calculate average
    add t1, t0, t1
    srai t1, t1, 1

    # combine components into final color
    slli t1, t1, 16 # R << 16
    slli t2, t2, 8  # G << 8

    or t5, t1, t2   # t4 <- RG0
    or t5, t5, t3   # t4 <- RGB

    j render_cursor

flip_red:
    li t0, 0x00ff0000
    xor t5, t5, t0
    j render_cursor


flip_green:
    li t0, 0x0000FF00
    xor t5, t5, t0
    j render_cursor


flip_blue:
    li t0, 0x000000FF
    xor t5, t5, t0
    j render_cursor


drop_color:
    # copying cursor color to cell cursor
    or t4, t5, x0
    j read_next_input

move_up:
    li t3, 124
    blt t6, t3, read_next_input

    jal ra, clear_cursor
    addi t6, t6, -128
    j move_cursor


move_down:
    li t3, 3968
    bge t6, t3, read_next_input

    jal ra, clear_cursor
    addi t6, t6, 128
    j move_cursor


move_left:
    # doing nothing if we are on the edge (living on the edge!!!!)
    andi t3, t6, 127
    beq t3, x0, read_next_input

    jal ra, clear_cursor
    addi t6, t6, -4
    j move_cursor


move_right:
    addi t3, t6, 4
    andi t3, t3, 127
    beq t3, x0, read_next_input
    
    jal ra, clear_cursor
    addi t6, t6, 4
    j move_cursor

render_cursor:
    la t0, display

    # adding offset to the base address of the display
    add t0, t0, t6

    sw t5, 0(t0)
    j read_next_input

move_cursor:
    la t0, display

    # adding offset to the base address of the display
    add t0, t0, t6

    # reading color of target cell into t4
    lw t4, 0(t0)

    sw t5, 0(t0)
    j read_next_input

# to erase cursor color and restore cell color
clear_cursor:
    la t0, display

    # adding offset to the base address of the display
    add t0, t0, t6

    sw t4, 0(t0)
    jr ra
