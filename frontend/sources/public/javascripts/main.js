document.getElementById("Step").addEventListener("click", () => {postAction("STEP")});
document.getElementById("Restart").addEventListener("click", () => {postAction("RESTART")});

document.getElementById("AllBack").addEventListener("click", () => {stepToStart()});
document.getElementById("Back").addEventListener("click", () => {stepBack()});
document.getElementById("Forward").addEventListener("click", () => {stepForward()});
document.getElementById("AllForward").addEventListener("click", () => {stepToEnd()});

document.getElementById("MemBack").addEventListener("click", () => {stepMemBack()});
document.getElementById("MemForward").addEventListener("click", () => {stepMemForward()});

document.getElementById("Run").addEventListener("click", () => {postAction("RUN", document.getElementById("delay-input").value, document.getElementById("cycle-input").value)});

keyboard_enabled = false;
const allowedKeys = [
    // Letters
    ...'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'.split(''),
    // Digits
    ...'0123456789'.split(''),
    // Symbols
    ' ', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '=', '+', '[', ']', '{', '}', '\\', '|', ';', ':', "'", '"', ',', '<', '.', '>', '/', '?', '`', '~'
];
document.getElementById("keyboard-switch-label").checked = false;
document.getElementById("keyboard-switch").addEventListener("click", () => {toggleKeyboard()});
function toggleKeyboard() {
    keyboard_enabled = document.getElementById("keyboard-switch").checked;
    document.getElementById("keyboard-switch-label").innerText = keyboard_enabled ? "Disable Keyboard" : "Enable Keyboard";
}

addEventListener('keydown', function(e) {
    if (!keyboard_enabled || !allowedKeys.includes(e.key)) return;
    if (document.getElementById("cycle-input") === document.activeElement || document.getElementById("delay-input") === document.activeElement) return;
    e.preventDefault();

    document.getElementById("terminal-input").innerText += e.key;
    postKey(e.key);
});

function postAction(actionName, delay=0, cycles=1) {
    fetch(location.protocol + '//' + location.hostname + ':' + location.port + '/new-action', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ action: actionName, delay: delay, cycles: cycles}),
    });
}

function postKey(inputValue) {
    fetch(location.protocol + '//' + location.hostname + ':' + location.port + '/new-key', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ key: inputValue })
    });
}


document.getElementById("Upload").addEventListener('click', function() {
    document.getElementById("configInput").click();
  });

  document.getElementById("configInput").addEventListener('change', function(event) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = function(e) {
        try {
          const jsonContent = JSON.parse(e.target.result);
          console.log(jsonContent);
          fetch(location.protocol + '//' + location.hostname + ':' + location.port + '/set-config', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ config: jsonContent })
          });
        } catch (error) {
          alert('Invalid JSON file');
        }
      };
      reader.readAsText(file);
    }
    document.getElementById("configInput").value = '';
  });

  function processError(message) {
    console.log(message);
    document.getElementById("error-message").innerText = message;
    document.getElementById("error-message").style.display = "block";
    setTimeout(function() {
      var alertElement = document.getElementById('error-message');
      if (alertElement) {
          alertElement.style.display = 'none';
      }
    }, 10000);
    state = null;
    drawUI();
  }