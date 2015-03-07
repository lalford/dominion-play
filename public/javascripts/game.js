var game = {};
var kingdomBoard = {};

function handleGameChange(evt) {
    game = $.parseJSON(evt.data);

    if ($.isEmptyObject(kingdomBoard)) {
        var chosenSetId = 1;
        var chosenSetIndex = 0;
        var chosenCards = recommendedBySet[chosenSetId][chosenSetIndex]["cards"];

        $.map(chosenCards, function(name, i) {
            var chosenCard = $.grep(cards, function(card) { return card["name"] == name; })[0];
            var quantity = 0;

            if (typeof chosenCard["isVictory"] === "undefined") {
                quantity = 10;
            } else {
                quantity = 12;
            }

            kingdomBoard[name] = {
                "cost" : chosenCard["cost"],
                "quantity" : quantity
            };
        });

        game["gameBoard"]["kingdomBoard"] = kingdomBoard;
    }

    console.log("new game state: \n" + game);
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