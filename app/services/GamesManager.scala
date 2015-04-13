package services

import akka.actor.ActorRef
import models.cards.{Victory, Treasure, Card}
import models.games._
import models.players.Player

import scala.collection.concurrent.TrieMap
import scala.util.Random

object GamesManager {
  private val games: TrieMap[String, Game] = TrieMap()

  def get(owner: String): Option[Game] = games.get(owner)

  def putIfAbsent(owner: String, game: Game): Option[Game] = games.putIfAbsent(owner, game)

  def openGames: TrieMap[String, Game] = games.filter { case (_, game) => game.state == WaitingForPlayers }

  def addPlayerHandle(owner: String, player: String, playerSocket: ActorRef): Unit = {
    synchronizedGameWork(owner) { game =>
      game.playerHandles.get(player) match {
        case Some(playerHandle) =>
          val newPlayerHandle = playerHandle.copy(gameSocket = Option(playerSocket))
          game.playerHandles.update(player, newPlayerHandle)
          updateGameStateForNewPlayer(game)
        case None =>
          if (game.playerHandles.size < game.numPlayers) {
            val playerHandle = PlayerHandle(
              gameSocket = Option(playerSocket),
              seat = game.playerHandles.size,
              player = PlayersManager.withStartingHand(player)
            )
            game.playerHandles.put(player, playerHandle)
            updateGameStateForNewPlayer(game)
          } else
            throw new IllegalStateException(s"sorry, someone clicked faster than $player")
      }
    }
  }

  private def updateGameStateForNewPlayer(game: Game): Unit = {
    if (game.playerHandles.size == game.numPlayers) {
      val disconnectedPlayers = game.playerHandles.values.filter(_.gameSocket.isEmpty)
      if (disconnectedPlayers.isEmpty)
        games.replace(game.owner, game.copy(state = Playing))
      else
        games.replace(game.owner, game.copy(state = Paused))
    } else {
      // still waiting
    }
  }

  def addNewKingdomBoard(owner: String, kingdomBoard: KingdomBoard): Unit = {
    synchronizedGameWork(owner) { game =>
      val newGameBoard = game.gameBoard.copy(kingdomBoard = kingdomBoard)
      val newGame = game.copy(gameBoard = newGameBoard)
      games.replace(owner, newGame)
    }
  }

  def disconnectPlayer(owner: String, player: String): Unit = {
    synchronizedGameWork(owner) { game =>
      val playerHandle = game.playerHandles.get(player)
      playerHandle.foreach(handle => game.playerHandles.update(player, handle.copy(gameSocket = None)))
    }
  }

  def gameBroadcast(owner: String): Unit = {
    synchronizedGameWork(owner) { game =>
      game.playerHandles.foreach { case (_, handle) => handle.gameSocket.foreach(_ ! game) }
    }
  }

  private def synchronizedGameWork(owner: String)(work: Game => Unit): Unit = {
    games.get(owner) match {
      case None => throw new IllegalStateException(s"owner: $owner game not found")
      case Some(game) => game.synchronized(work(game))
    }
  }
}

object PlayersManager {
  private val registeredPlayers: TrieMap[String, Player] = TrieMap()

  def get(name: String): Option[Player] = registeredPlayers.get(name)
  def putIfAbsent(name: String, player: Player): Option[Player] = registeredPlayers.putIfAbsent(name, player)
  def update(name: String, player: Player): Unit = registeredPlayers.update(name, player)

  def withStartingHand(name: String): Player = {
    val player = get(name).getOrElse(throw new IllegalStateException(s"player: $name not found"))
    val coppers = (1 to 7 inclusive).map(_ => new Card("Copper", 0, Set(Treasure(1))))
    val estates = (1 to 3 inclusive).map(_ => new Card("Estate", 2, Set(Victory(1))))
    val (hand, deck) = Random.shuffle(coppers ++ estates).splitAt(5)

    update(name, player.copy(isConnected = true))
    hand.foreach(player.hand += _)
    deck.foreach(player.deck += _)
    player
  }
}
