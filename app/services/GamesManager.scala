package services

import akka.actor.ActorRef
import models.cards.{Victory, Treasure, Card}
import models.games.{Playing, PlayerHandle, WaitingForPlayers, Game}
import models.players.Player

import scala.collection.concurrent.TrieMap
import scala.util.Random

object GamesManager {
  private val games: TrieMap[String, Game] = TrieMap()

  def get(owner: String): Option[Game] = games.get(owner)
  def putIfAbsent(owner: String, game: Game): Option[Game] = games.putIfAbsent(owner, game)

  def openGames: TrieMap[String, Game] = games.filter { case (_, game) => game.state == WaitingForPlayers }

  def addPlayerHandle(owner: String, player: String, playerSocket: ActorRef): Unit = {
    games.get(owner) match {
      case None => throw new IllegalStateException(s"owner: $owner game not found")
      case Some(game) =>
        game.synchronized {
          // TODO - fix edge case where reconnecting player gets duplicated starting hand
          if (game.playerHandles.size < game.numPlayers) {
            val playerHandle = PlayerHandle(
              gameSocket = playerSocket,
              seat = game.playerHandles.size,
              player = PlayersManager.withStartingHand(player)
            )
            game.playerHandles.put(player, playerHandle)

            if (game.playerHandles.size == game.numPlayers)
              games.replace(owner, game.copy(state = Playing))
          } else
            throw new IllegalStateException(s"sorry, someone clicked faster than $player")
        }
    }
  }

  def gameBroadcast(owner: String): Unit = {
    games.get(owner) match {
      case Some(game) => game.playerHandles.foreach { case (_, handle) => handle.gameSocket ! game }
      case _ =>
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
