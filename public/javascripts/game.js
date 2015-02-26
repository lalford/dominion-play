function handleGameStateChange(evt) {
    var changedGame = evt.data;
    console.log("game state changes need client implementation: \n" + changedGame);
}

function connectToGame(owner, player, path) {
    var loc = window.location;
    var host = loc.host;

    var gameOwner = owner;
    var activePlayer = player;
    var gameSocket = new WebSocket("ws://" + host + path);

    gameSocket.onopen = function() {
        var connectedEvent = {
            "eventType": "Connected",
            "gameOwner": gameOwner,
            "player": activePlayer
        }
        gameSocket.send(JSON.stringify(connectedEvent));
        console.log(activePlayer + " joined " + gameOwner);
    };

    gameSocket.onmessage = handleGameStateChange;

    gameSocket.onclose = function() {
        alert(activePlayer + " disconnected from " + gameOwner);
    };
}