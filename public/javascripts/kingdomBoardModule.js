var kingdomBoardModule = (function() {
    var gameConnectionInfo;

    var assignQuantities = function(chosenCards) {
        var kingdomBoard = [];
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

        return kingdomBoard;
    };

    var getRandomInt = function(min, max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    };

    var newBoard = function() {
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

        return assignQuantities(chosenCards);
    };

    return {
        init: function(gci) {
            gameConnectionInfo = gci;
        },
        newKingdomBoardEvent: function() {
            return {
                "eventType": "New Kingdom Board",
                "gameOwner": gameConnectionInfo.owner,
                "player": gameConnectionInfo.player,
                "kingdomBoard": newBoard()
            };
        },
        draw: function(kingdomBoard) {
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
                    kingdomBoardRow1Elem.append(drawUtilsModule.cardDisplay(card));
                } else {
                    kingdomBoardRow2Elem.append(drawUtilsModule.cardDisplay(card));
                }

                count++;
            });

            var kingdomBoardElem = $(".kingdom-board");
            kingdomBoardElem.empty();
            kingdomBoardElem.append(kingdomBoardRow1Elem);
            kingdomBoardElem.append(kingdomBoardRow2Elem);
        }
    }
})();