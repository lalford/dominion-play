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

function allPlayersDisplay(players) {
    var playersTable = $('<table>');
    var headerRow = $('<tr>');
    var playerHeader = $('<th>', { text: "Player" });
    var handHeader = $('<th>', { text: "Hand" });
    var deckHeader = $('<th>', { text: "Deck" });
    var discardHeader = $('<th>', { text: "Discard" });
    var totalHeader = $('<th>', { text: "Total" });

    headerRow.append(playerHeader);
    headerRow.append(handHeader);
    headerRow.append(deckHeader);
    headerRow.append(discardHeader);
    headerRow.append(totalHeader);

    playersTable.append(headerRow);

    $.each(players, function (_, player) {
        var playerRow = $('<tr>');
        var name = $('<td>', { text: player["name"] });
        var handCount = $('<td>', { text: player["hand"].length });
        var deckCount = $('<td>', { text: player["deck"].length });
        var discardCount = $('<td>', { text: player["discard"].length });
        var totalCount = $('<td>', { text: player["total"] });

        playerRow.append(name);
        playerRow.append(handCount);
        playerRow.append(deckCount);
        playerRow.append(discardCount);
        playerRow.append(totalCount);

        playersTable.append(playerRow);
    });

    return playersTable;
}

function drawGame(game) {
    console.log("game json to draw:");
    console.log(JSON.stringify(game));

    var state = game["state"];
    var victoryBoard = game["gameBoard"]["victoryBoard"];
    var treasureBoard = game["gameBoard"]["treasureBoard"];
    var kingdomBoard = game["gameBoard"]["kingdomBoard"];
    var players = game["players"].sort(function (a, b) {
        return ((a["seat"] < b["seat"]) ? -1 : ((a["seat"] > b["seat"]) ? 1 : 0));
    });

    $(".status-board").html("Status: " + state);

    var victoryBoardElem = $(".victory-board");

    victoryBoardElem.empty();
    $.each(victoryBoard, function(cardName, card) {
        victoryBoardElem.append(cardDisplay(card));
    });

    var treasureBoardElem = $(".treasure-board");

    treasureBoardElem.empty();
    $.each(treasureBoard, function(cardName, card) {
        treasureBoardElem.append(cardDisplay(card));
    });

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

    var allPlayersBoard = $(".all-players-board");
    allPlayersBoard.empty();
    allPlayersBoard.append(allPlayersDisplay(players));

}

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function handleGameChange(evt) {
    var game = $.parseJSON(evt.data);
    var kingdomBoard = game["gameBoard"]["kingdomBoard"];

    if ($.isEmptyObject(kingdomBoard)) {
        var chosenSetId = getRandomInt(1,10);
        var chosenSetGames = recommendedBySet[chosenSetId];
        var chosenSetIndex = getRandomInt(0, chosenSetGames.length - 1);
        var chosenCards = [];

        if ($.isEmptyObject(chosenSetGames)) {
            chosenCards = recommendedBySet[1][0]["cards"];
        } else {
            var chosenGame = chosenSetGames[chosenSetIndex];
            if ($.isEmptyObject(chosenGame)) {
                chosenCards = recommendedBySet[1][0]["cards"];
            } else {
                chosenCards = chosenGame["cards"];
            }
        }

        kingdomBoard = [];
        $.map(chosenCards, function(name, i) {
            var chosenCard = $.grep(cards, function(card) { return card["name"] == name; })[0];
            var quantity = 0;

            if (typeof chosenCard["isVictory"] === "undefined") {
                quantity = 10;
            } else {
                quantity = 12;
            }

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