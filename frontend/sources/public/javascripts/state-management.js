state = null;
memory_base_address = 0x00000000;

is_live = true;
current_state_index = 0;

function initDisplay() {
    state.pixels = [];
    const display = document.getElementById('display-output');
    display.innerHTML = '';
    for (let i = 0; i < 32 * 32; i++) {
        const pixel = document.createElement('div');
        pixel.classList.add('pixel');
        display.appendChild(pixel);
        state.pixels.push(pixel);
    }
}

function processInitialState(initial_state) {
    state = {}
    state.initial_state = initial_state;
    state.updates = [];
    is_live = true;
    current_state_index = 0;
    keyboard_enabled = false;
    document.getElementById("keyboard-switch").checked = false;
    document.getElementById("terminal-input").innerText = "";

    computeState();
}

function processUpdate(update) {
    state.updates.push(update);
    if (is_live) {
        applyUpdate(update);
    }
}

function processState(current_state) {
    if (state == null) {
        state = current_state;
        is_live = true;
        current_state_index = 0;
        keyboard_enabled = false;
        document.getElementById("keyboard-switch").checked = false;
        computeState();
        return true;
    }
    return false;
}
function stepToStart() {
    while (current_state_index > 0) {
        stepBack(false);
    }
    updateUI();
}
function stepBack(needsUpdate=true) {
    if (current_state_index > 0) {
        unApplyUpdate(state.updates.find(update => update.index == current_state_index));
        current_state_index--;
        is_live = false;
    }
    if (needsUpdate) {
        updateUI();
    }
}

function stepForward(needsUpdate=true) {
    if (current_state_index == state.updates.length) {
        is_live = true;
        postAction("STEP", 0, 1);
    }
    if (current_state_index < state.updates.length) {
        current_state_index++;
        applyUpdate(state.updates.find(update => update.index == current_state_index));
        is_live = current_state_index == state.updates.length;
    }
    if (needsUpdate) {
        updateUI();
    }
}

function stepToEnd() {
    if (current_state_index == state.updates.length) {
        is_live = true;
        postAction("STEP", 0, 1);
    }
    while (current_state_index < state.updates.length) {
        stepForward(false);
    }
    updateUI();
}

function computeState() {
    if (!state.pixels) {
        initDisplay();
    }
    state.current_state = state.initial_state;
    state.current_state.terminal_output = "";
    state.updates.forEach(update => {
        applyUpdate(update);
    });
}

function applyUpdate(update) {
    current_state_index = update.index
    state.current_state.pc = update.pc.new_value;
    state.current_state.terminal_output = update.terminal_output.new_value;
    for (const [registerNumber, registerUpdate] of Object.entries(update.register_updates)) {
        state.current_state.registers[registerNumber] = registerUpdate.new_value;
        updateRegisterValue(registerNumber, registerUpdate.new_value);
    }
    for (const [csrName, csrUpdate] of Object.entries(update.csr_updates)) {
        state.current_state.csrs[csrName] = csrUpdate.new_value;
        updateCSRValue(csrName, csrUpdate.new_value);
    }
    for (const [address, value] of Object.entries(update.memory_updates)) {
        state.current_state.memory[address] = value.new_value;
        updateMemoryValue(address, value.new_value);
    }
    for (const [address, color] of Object.entries(update.display_output)) {
        const match = address.match(/\((\d+),\s*(\d+)\)/);
        if (match) {
            const x = parseInt(match[1], 10);
            const y = parseInt(match[2], 10);
            setPixel(x, y, `#${color.new_value.toString(16).padStart(6, '0')}`);
        }
    }
}

function unApplyUpdate(update) {
    state.current_state.pc = update.pc.old_value;
    state.terminal_output = update.terminal_output.old_value;
    for (const [registerNumber, registerUpdate] of Object.entries(update.register_updates)) {
        state.current_state.registers[registerNumber] = registerUpdate.old_value;
        updateRegisterValue(registerNumber, registerUpdate.old_value);
    }
    for (const [csrName, csrUpdate] of Object.entries(update.csr_updates)) {
        state.current_state.csrs[csrName] = csrUpdate.old_value;
        updateCSRValue(csrName, csrUpdate.old_value);
    }
    for (const [address, value] of Object.entries(update.memory_updates)) {
        state.current_state.memory[address] = value.old_value;
        updateMemoryValue(address, value.old_value);
    }
    for (const [address, color] of Object.entries(update.display_output)) {
        const match = address.match(/\((\d+),\s*(\d+)\)/);
        if (match) {
            const x = parseInt(match[1], 10);
            const y = parseInt(match[2], 10);
            setPixel(x, y, `#${color.old_value.toString(16).padStart(6, '0')}`);
        }
    }
}

function storeBase(base) {
    if (base < 0x00000000) {
        base = 0x00000000;
    }
    if (base > 0xffffff00) {
        base = 0xffffff00;
    }
    memory_base_address = base;
    drawMemoryTab(base);
}

function stepMemBack() {
    if (memory_base_address > 0x00000100) {
        memory_base_address -= 0x00000100;
    } else {
        memory_base_address = 0x00000000;
    }
    drawMemoryTab(memory_base_address);
}

function stepMemForward() {
    if (memory_base_address < 0xffffff00) {
        memory_base_address += 0x00000100;
    } else {
        memory_base_address = 0xffffff00;
    }
    drawMemoryTab(memory_base_address);
}

function setPixel(x, y, color) {
    if (x < 0 || x >= 32 || y < 0 || y >= 32) return;
    const index = y * 32 + x;
    state.pixels[index].style.backgroundColor = color;
}