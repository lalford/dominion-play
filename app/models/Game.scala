package models

import akka.actor.ActorRef
import play.api.libs.json.Json
import play.api.mvc.WebSocket.FrameFormatter

import scala.collection.concurrent.TrieMap

case class Game(
  owner: String,
  numPlayers: Int,
  state: GameState,
  gameBoard: GameBoard,
  playerHandles: TrieMap[String, PlayerHandle]
)

case class PlayerHandle(
  gameSocket: ActorRef,
  seat: Int,
  player: Player
)

sealed trait GameState
case object WaitingForPlayers extends GameState
case object Playing extends GameState

sealed trait GameEvent {
  def owner: String
  def player: String
}

case class Connected(owner: String, player: String) extends GameEvent

object GameEventSerializers {
  implicit val connectedFormat = Json.format[Connected]
  implicit val connectedFrameFormatter = FrameFormatter.jsonFrame[Connected]
}