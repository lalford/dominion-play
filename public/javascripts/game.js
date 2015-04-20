var gameConnectionInfo = {};

function handleGameChange(evt) {
    var game = $.parseJSON(evt.data);
    var kingdomBoard = game["gameBoard"]["kingdomBoard"];

    if ($.isEmptyObject(kingdomBoard)) {
        var newKingdomBoardEvent = kingdomBoardModule.newKingdomBoardEvent();
        gameConnectionInfo.socket.send(JSON.stringify(newKingdomBoardEvent));
    } else {
        drawGameModule.draw(game);
    }
}

function connectToGame(owner, player, path, menu) {
    var loc = window.location;
    var host = loc.host;

    gameConnectionInfo.owner = owner;
    gameConnectionInfo.player = player;
    gameConnectionInfo.socket = new WebSocket("ws://" + host + path);

    gameModules = [
        kingdomBoardModule,
        drawGameModule,
        leaveGameModule,
        statusModule,
        victoryBoardModule,
        treasureBoardModule,
        playersBoardModule
    ];
    $.each(gameModules, function(_, gameModule) {
        gameModule.init(gameConnectionInfo);
    });

    gameConnectionInfo.socket.onopen = function() {
        var connectEvent = {
            "eventType": "Connect",
            "gameOwner": gameConnectionInfo.owner,
            "player": gameConnectionInfo.player
        };
        gameConnectionInfo.socket.send(JSON.stringify(connectEvent));
        console.log(gameConnectionInfo.player + " joined " + gameConnectionInfo.owner);
    };

    gameConnectionInfo.socket.onmessage = handleGameChange;

    gameConnectionInfo.socket.onclose = function() {
        loc.assign(menu);
    };
}