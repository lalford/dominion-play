var gameOwner = null;
var activePlayer = null;
var gameSocket = null;

function handleGameChange(evt) {
    var game = $.parseJSON(evt.data);
    var kingdomBoard = game["gameBoard"]["kingdomBoard"];

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

            kingdomBoard = [];
            kingdomBoard.push({
                "card" : name,
                "cost" : chosenCard["cost"],
                "quantity" : quantity
            });
        });

        var newKingdomBoardEvent = {
            "eventType": "New Kingdom Board",
            "gameOwner": gameOwner,
            "player": activePlayer,
            "kingdomBoard": kingdomBoard
        };

        gameSocket.send(JSON.stringify(newKingdomBoardEvent));
    } else {
        console.log("got a full game, draw the shit!\n" + JSON.stringify(game));
    }
}

function connectToGame(owner, player, path) {
    var loc = window.location;
    var host = loc.host;

    gameOwner = owner;
    activePlayer = player;
    gameSocket = new WebSocket("ws://" + host + path);

    gameSocket.onopen = function() {
        var connectEvent = {
            "eventType": "Connect",
            "gameOwner": gameOwner,
            "player": activePlayer
        };
        gameSocket.send(JSON.stringify(connectEvent));
        console.log(activePlayer + " joined " + gameOwner);
    };

    gameSocket.onmessage = handleGameChange;

    gameSocket.onclose = function() {
        alert(activePlayer + " disconnected from " + gameOwner);
    };
}