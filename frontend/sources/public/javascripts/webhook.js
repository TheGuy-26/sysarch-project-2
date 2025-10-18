var ws = new WebSocket('ws://' + location.hostname + ':' + location.port + '/socket');

setInterval(function() {
    ws.send("Heartbeat");
}, 5000);

ws.onmessage = function (event) {
    var update = JSON.parse(event.data);
    console.log(update);
    switch (update.type) {
        case "INITIAL-STATE":
            processInitialState(update.state);
            drawUI();
            break;
        case "STATE-UPDATE":
            processUpdate(update.update);
            updateUI();
            break;
        case "CURRENT-STATE":
            if (processState(update.state)) {
                drawUI();
            }
            break;
        case "ERROR-MESSAGE":
            console.log(update.message);
            processError(update.message);
            break;
    }
};

function fetchCurrentState() {
    fetch(location.protocol + '//' + location.hostname + ':' + location.port + '/get-current-state')
        .then(response => {
            console.log(response);
            if (!response.ok) {
                throw new Error();
            }
            return response.json();
        })
        .then(data => {
            if (data == "") {
                return;
            }
            state = data;
            computeState();
            drawUI();  
        })
        .catch(error => {
            console.log(error)
            state = null;
            drawUI();  
        });  
}

function fetchAvailableConfigs() {
    fetch(location.protocol + '//' + location.hostname + ':' + location.port + '/get-available-configs')
        .then(response => {
            if (!response.ok) {
                throw new Error();
            }
            return response.json();
        })
        .then(data => {
            available_configs = data;
            console.log("Available configs:", data);
            const select = document.getElementById("configDropdown");
            select.innerHTML = ""; // Clear existing options
            data.sort((a, b) => a.id.localeCompare(b.id));
            data.forEach(config => {
                const option = document.createElement("option");
                option.value = config.id;
                option.textContent = config.name;
                option.id = config.id;
                select.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Error fetching available configs:', error);
        });
}