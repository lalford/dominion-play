var gameOwner = null;
var activePlayer = null;
var gameSocket = null;

function cardDisplay(card) {
    var format = card["card"] + " $" + card["cost"] + " (" + card["quantity"] + ")";
    return $('<p>', {
        class: "card-display",
        text: format
    });
}

function drawGame(game) {
    console.log("game json to draw:");
    console.log(JSON.stringify(game));

    $(".status-board").html("Status: " + game["state"]);

    var victoryBoard = game["gameBoard"]["victoryBoard"];
    var victoryBoardElem = $(".victory-board");

    victoryBoardElem.empty();
    $.each(victoryBoard, function(cardName, card) {
        victoryBoardElem.append(cardDisplay(card));
    });

    var treasureBoard = game["gameBoard"]["treasureBoard"];
    var treasureBoardElem = $(".treasure-board");

    treasureBoardElem.empty();
    $.each(treasureBoard, function(cardName, card) {
        treasureBoardElem.append(cardDisplay(card));
    });

    var kingdomBoard = game["gameBoard"]["kingdomBoard"];
    var kingdomBoardRow1Elem = $("<div>", {
        class: "kingdom-board-row"
    });
    var kingdomBoardRow2Elem = $("<div>", {
        class: "kingdom-board-row"
    });

    var numKingdoms = 0;
    $.each(kingdomBoard, function(cardName, card) {
        numKingdoms++;
    });

    var count = 0;
    $.each(kingdomBoard, function(cardName, card) {
        if (count < numKingdoms / 2) {
            kingdomBoardRow1Elem.append(cardDisplay(card));
        } else {
            kingdomBoardRow2Elem.append(cardDisplay(card));
        }

        count++;
    });

    var kingdomBoardElem = $(".kingdom-board");
    kingdomBoardElem.empty();
    kingdomBoardElem.append(kingdomBoardRow1Elem);
    kingdomBoardElem.append(kingdomBoardRow2Elem);
}

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
        drawGame(game);
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