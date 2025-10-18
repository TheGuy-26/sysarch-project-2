previous_active_tab = "";

function uninitializedMessage() {
    if (!state) {
        document.getElementById('content').style.display = 'none';
        document.getElementById('uninitialized-message').style.display = 'block';
        return false;
    } else {
        document.getElementById('content').style.display = 'block';
        document.getElementById('uninitialized-message').style.display = 'none';
        return true;
    }
}

function drawUI() {
    if (uninitializedMessage()) {
        drawSourceCode();
        drawRegister();
        drawCSR();
        drawMemoryTab(memory_base_address);
        updateUI();
        return;
    }
}
function drawSourceCodeTab(name, displayName) {
    html = ""
    html += '<li class="nav-item" role="presentation">'
    html += '    <button class="nav-link code-tab" id="' + name + '-tab" data-bs-toggle="tab" data-bs-target="#' + name + '" type="button" role="tab" aria-controls="' + name + '" aria-selected="false">' + displayName + '</button>'
    html += '</li>'
    return html
}

function drawSourceCodeTabContent(name, program) {
    html = ""
    html += '<div class="tab-pane fade code-tab-content" id="' + name + '" role="tabpanel" aria-labelledby="' + name + '-tab">'
    html += '<span id="' + name + '-span" role="tabpanel" aria-labelledby="' + name + '-tab">'
    html += '<table class="table table-striped table-striped-custom">'
    html += '  <thead>'
    html += '    <tr>'
    html += '      <th>Address</th>'
    html += '      <th>Label</th>'
    html += '      <th>Instruction</th>'
    html += '    </tr>'
    html += '  </thead>'
    html += '  <tbody>'
    for (const [address, instruction] of Object.entries(program.instructions)) {
        actual_address = parseInt(address)
        html += '    <tr>'
        html += '      <td class="code" id="' + decToHex(actual_address) + '-address">' + decToHex(actual_address) + '</td>'
        html += '      <td class="code" id="' + decToHex(actual_address) + '-label">' + (program.labels[actual_address] ?? '') + '</td>'
        html += '      <td class="code" id="' + decToHex(actual_address) + '-instruction">' + decToHex(instruction)
        if (program.assembly[address]) {
            html += ' (' + hljs.highlight(program.assembly[address], { language: 'riscvasm' }).value.trim() + ')'
        }
        html += '</td>'
        html += '    </tr>'
    };
    html += '  </tbody>'
    html += '</table>'
    html += '</span>'
    html += '</div>'
    return html
}

function drawSourceCode() {
    previous_active_tab = "";
    html = ""
    html += '<ul class="nav nav-tabs mb-3" role="tablist">'
    html += drawSourceCodeTab("machine", state.initial_state.machine_program.name)
    state.initial_state.user_programs.forEach((program, index) => {
        html += drawSourceCodeTab("user-" + index, program.name)
    });
    html += '</ul>'
    html += '<div class="tab-content">'
    html += drawSourceCodeTabContent("machine", state.initial_state.machine_program)
    state.initial_state.user_programs.forEach((program, index) => {
        html += drawSourceCodeTabContent("user-" + index, program)
    });
    html += '</div>'
    document.getElementById("source-code").innerHTML = html
    var currentline = document.getElementById("current-line")
    if (currentline != null) {
        currentline.scrollIntoView({ behavior: "auto", block: "nearest" });
    }
}

function drawRegister() {
    registerHtml = ""
    registerHtml += '<table class="table table-striped table-striped-custom">';
    registerHtml += '  <thead>';
    registerHtml += '    <tr>';
    registerHtml += '      <th>Name</th>';
    registerHtml += '      <th>Number</th>';
    registerHtml += '      <th>Content</th>';
    registerHtml += '    </tr>';
    registerHtml += '  </thead>';
    registerHtml += '  <tbody>';
    for (const [number, value] of Object.entries(state.current_state.registers)) {
        registerHtml += '    <tr>';
        registerHtml += '      <td>' + registerNameFromNumber(number) + '</td>';
        registerHtml += '      <td>' + 'x' + number + '</td>';
        registerHtml += '      <td id="register-x' + number + '">' + decToHex(value) + '</td>';
        registerHtml += '    <tr>';
    }
    registerHtml += '  </tbody>';
    registerHtml += '</table>';
    document.getElementById("register").innerHTML = registerHtml;
}

function drawCSR() {
    csrHtml = ""
    csrHtml += '<table class="table table-striped table-striped-custom">';
    csrHtml += '  <thead>';
    csrHtml += '    <tr>';
    csrHtml += '      <th>Name</th>';
    csrHtml += '      <th>Address</th>';
    csrHtml += '      <th>Content</th>';
    csrHtml += '    </tr>';
    csrHtml += '  </thead>';
    csrHtml += '  <tbody>';
    for (const [address, value] of Object.entries(state.current_state.csrs)) {
        if (csrAddressToName(address) == null) {
            continue;
        }
        csrHtml += '    <tr>';
        csrHtml += '      <td>' + csrAddressToName(address) + '</td>';
        csrHtml += '      <td>' + decToHexLength(parseInt(address), 3) + '</td>';
        csrHtml += '      <td id="csr-' + csrAddressToName(address) + '">' + decToHex(value) + '</td>';
        csrHtml += '    <tr>';
    }
    csrHtml += '  </tbody>';
    csrHtml += '</table>';
    document.getElementById("csr").innerHTML = csrHtml;
}

function drawMemoryTab(base) {
    document.getElementById("memory-base").innerText = decToHex(base);
    html = ''
    html += '<table class="table table-striped table-striped-custom">'
    html += '  <thead>'
    html += '    <tr>'
    html += '      <th>Address</th>'
    html += '      <th>Value</th>'
    html += '      <th>Value + 0x4</th>'
    html += '      <th>Value + 0x8</th>'
    html += '      <th>Value + 0xC</th>'
    html += '      <th>Value + 0x10</th>'
    html += '      <th>Value + 0x14</th>'
    html += '      <th>Value + 0x18</th>'
    html += '      <th>Value + 0x1C</th>'
    html += '    </tr>'
    html += '  </thead>'
    html += '  <tbody>'
    for (let i = 0; i < 64; i += 8) {
        html += '    <tr>'
        html += '      <td>' + decToHex(base + (i * 4)) + '</td>'
        for (let j = 0; j < 8; j++) {
            html += '      <td id="mem-' + decToHex(base + (i * 4) + (j * 4)) + '">' + decToHex(state?.current_state?.memory[base + (i * 4) + (j * 4)] ?? 0) + '</td>'
        }
        html += '    </tr>'
    }
    html += '  </tbody>'
    html += '</table>'
    document.getElementById("memory-content").innerHTML = html;
}


function updateUI() {
    if (uninitializedMessage()) {
        setActiveSourceCodeTab();
        setActiveInstruction();
        updateTerminal();
        updateStateCounter();
    }
}

function updateStateCounter() {
    document.getElementById("state-counter").innerText = "" + (current_state_index + 1) + "/" + (state.updates.length + 1);
}

function updateTerminal() {
    document.getElementById("terminal-output").innerText = state.current_state.terminal_output;
    return;
}

function setActiveSourceCodeTab() {
    var name = state.initial_state.machine_program.instructions[state.current_state.pc] != undefined ? "machine" : "user-" + state.initial_state.user_programs.findLastIndex(program => program.instructions[state.current_state.pc] != undefined);
    if (previous_active_tab == name) {
        return;
    }
    previous_active_tab = name;
    var tab = document.getElementById(name + '-tab');
    if (tab != null) {
        if (tab.classList.contains('active')) {
            return;
        }
        var tabs = document.querySelectorAll('.code-tab');
        if (tabs != null) {
            tabs.forEach(tab => {
                tab.classList.remove('active');
                tab.ariaSelected = false;
                tab.innerHTML = tab.innerHTML.replace('<i class="bi bi-play-circle"></i> ', '');
            });
        }
        tab.classList.add('active');
        tab.ariaSelected = true;
        tab.innerHTML = '<i class="bi bi-play-circle"></i> ' + tab.innerHTML;
    }
    var content = document.getElementById(name);
    if (content != null) {
        if (content.classList.contains('show')) {
            return;
        }
        var contents = document.querySelectorAll('.code-tab-content');
        if (contents != null) {
            contents.forEach(content => {
                content.classList.remove('show');
                content.classList.remove('active');
            });
        }
        content.classList.add('show');
        content.classList.add('active');
    }
}

function setActiveInstruction() {
    var actives = document.querySelectorAll('.current-line');
    if (actives.length > 0) {
        actives.forEach(active => {
            active.classList.remove('current-line');
        });
    }
    var address = state.current_state.pc;
    var ui_address = document.getElementById(decToHex(address) + '-address');
    if (ui_address != null) {
        ui_address.classList.add('current-line');
        ui_address.scrollIntoView({ behavior: "auto", block: "nearest" });
    }
    var ui_label = document.getElementById(decToHex(address) + '-label');
    if (ui_label != null) {
        ui_label.classList.add('current-line');
    }
    var ui_instruction = document.getElementById(decToHex(address) + '-instruction');
    if (ui_instruction != null) {
        ui_instruction.classList.add('current-line');
    }
}

function updateRegisterValue(number, value) {
    var ui = document.getElementById('register-x' + number);
    if (ui == null) {
        return;
    }
    ui.innerText = decToHex(value);
}

function updateCSRValue(address, value) {
    var ui = document.getElementById('csr-' + csrAddressToName(address));
    if (ui == null) {
        return;
    }
    ui.innerText = decToHex(value);
}

function updateMemoryValue(address, value) {
    var ui = document.getElementById('mem-' + decToHex(parseInt(address)));
    if (ui == null) {
        return;
    }
    ui.innerText = decToHex(value);
}




