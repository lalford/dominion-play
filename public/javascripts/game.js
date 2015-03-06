var game = {};
var kingdomBoard = {};

function handleGameChange(evt) {
    game = evt.data;

    if (jQuery.isEmptyObject(kingdomBoard)) {
        var chosenSetId = 1;
        var chosenSetIndex = 0;
        var chosenCards = recommendedBySet[chosenSetId][chosenSetIndex]["cards"];

        jQuery.map(chosenCards, function(name, i) {
            var chosenCard = jQuery.grep(cards, function(n) { return n == name; })[0];
            kingdomBoard[name] = {
                "cost" : chosenCard["cost"],
                "quantity" : chosenCard["quantity"]
            };
        });

        game["kingdomBoard"] = kingdomBoard;
    }

    console.log("game state changes need client implementation: \n" + game);
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

    gameSocket.onmessage = handleGameChange;

    gameSocket.onclose = function() {
        alert(activePlayer + " disconnected from " + gameOwner);
    };
}