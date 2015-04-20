var statusModule = (function() {
    var gameConnectionInfo;

    return {
        init: function(gci) {
            gameConnectionInfo = gci;
        },
        draw: function(state) {
            $(".status-board").html("Status: " + state);
        }
    }
})();

var leaveGameModule = (function() {
    var gameConnectionInfo;

    var leaveGame = function() {
        var leaveEvent = {
            "eventType": "Leave",
            "gameOwner": gameConnectionInfo.owner,
            "player": gameConnectionInfo.player
        };
        gameConnectionInfo.socket.send(JSON.stringify(leaveEvent));
        gameConnectionInfo.socket.close();
    };

    return {
        init: function(gci) {
            gameConnectionInfo = gci;
        },
        draw: function() {
            var leaveGameButton = $("<button>", {
                text: "Leave Game",
                click: leaveGame
            });

            $(".leave-game").html(leaveGameButton);
        }
    }
})();

var victoryBoardModule = (function() {
    var gameConnectionInfo;

    return {
        init: function(gci) {
            gameConnectionInfo = gci;
        },
        draw: function(victoryBoard) {
            var victoryBoardElem = $(".victory-board");
            victoryBoardElem.empty();
            $.each(victoryBoard, function(cardName, card) {
                victoryBoardElem.append(drawUtilsModule.cardDisplay(card));
            });
        }
    }
})();

var treasureBoardModule = (function() {
    var gameConnectionInfo;

    return {
        init: function(gci) {
            gameConnectionInfo = gci;
        },
        draw: function(treasureBoard) {
            var treasureBoardElem = $(".treasure-board");
            treasureBoardElem.empty();
            $.each(treasureBoard, function(cardName, card) {
                treasureBoardElem.append(drawUtilsModule.cardDisplay(card));
            });
        }
    }
})();

var playersBoardModule = (function() {
    var gameConnectionInfo;

    var allPlayersDisplay = function(players) {
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
    };

    return {
        init: function(gci) {
            gameConnectionInfo = gci;
        },
        draw: function(players) {
            var allPlayersBoard = $(".all-players-board");
            allPlayersBoard.empty();
            allPlayersBoard.append(allPlayersDisplay(players));
        }
    }
})();

var playerModule = (function() {
    var gameConnectionInfo;

    return {
        init: function(gci) {
            gameConnectionInfo = gci;
        },
        draw: function(players) {
            var player = {};
            for (var i = 0; i < players.length; i++) {
                if (players[i].name === gameConnectionInfo.player) {
                    player = players[i];
                    break;
                }
            }

            var playerBoard = $(".player-board");
            playerBoard.empty();
            playerBoard.append($('<p>', { text: "Your Hand" }));
            $.each(player["hand"], function(_, cardName) {
                playerBoard.append($('<p>', { text: cardName }));
            });
        }
    }
})();

var drawGameModule = (function() {
    var gameConnectionInfo;

    return {
        init: function(gci) {
            gameConnectionInfo = gci;
        },
        draw: function(game) {
            console.log("game json to draw:");
            console.log(JSON.stringify(game));

            var state = game["state"];
            var victoryBoard = game["gameBoard"]["victoryBoard"];
            var treasureBoard = game["gameBoard"]["treasureBoard"];
            var kingdomBoard = game["gameBoard"]["kingdomBoard"];
            var players = game["players"].sort(function (a, b) {
                return ((a["seat"] < b["seat"]) ? -1 : ((a["seat"] > b["seat"]) ? 1 : 0));
            });

            statusModule.draw(state);
            leaveGameModule.draw();
            victoryBoardModule.draw(victoryBoard);
            treasureBoardModule.draw(treasureBoard);
            kingdomBoardModule.draw(kingdomBoard);
            playersBoardModule.draw(players);
            playerModule.draw(players);
        }
    }
})();