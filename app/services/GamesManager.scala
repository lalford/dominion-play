package services

import akka.actor.ActorRef
import models.games.{Playing, PlayerHandle, WaitingForPlayers, Game}
import models.players.Player

import scala.collection.concurrent.TrieMap

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
          // TODO - handle rejoining after disconnect
          if (game.playerHandles.size < game.numPlayers) {
            // TODO - players need a starting deck
            val playerHandle = PlayerHandle(
              gameSocket = playerSocket,
              seat = game.playerHandles.size,
              player = PlayersManager.get(player).getOrElse(throw new IllegalStateException(s"player: $player not found"))
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
}
