package models.games

import akka.actor.ActorRef
import models.players.Player

import scala.collection.concurrent.TrieMap

sealed trait GameState
case object WaitingForPlayers extends GameState
case object Playing extends GameState
case object Paused extends GameState

case class PlayerHandle(
  gameSocket: Option[ActorRef],
  seat: Int,
  player: Player
)

case class Game(
  owner: String,
  numPlayers: Int,
  state: GameState,
  gameBoard: GameBoard,
  playerHandles: TrieMap[String, PlayerHandle]
)