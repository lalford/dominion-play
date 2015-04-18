package models.games

import models.players.PlayerHandle

import scala.collection.concurrent.TrieMap

sealed trait GameState
case object WaitingForPlayers extends GameState
case object Playing extends GameState
case object Paused extends GameState
case object GameFailed extends GameState

case class Game(
  owner: String,
  numPlayers: Int,
  state: GameState,
  gameBoard: GameBoard,
  playerHandles: TrieMap[String, PlayerHandle]
)