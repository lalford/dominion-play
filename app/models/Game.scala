package models

import akka.actor.ActorRef
import play.api.libs.json._
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

case class GameEvent(
  eventType: GameEventType,
  gameOwner: String,
  player: String
)

// any new type needs reads boilerplate
sealed trait GameEventType
case object Connected extends GameEventType

object GameSerializers {
  implicit object GameEventTypeFormat extends Format[GameEventType] {
    override def reads(json: JsValue): JsResult[GameEventType] = json match {
      case JsString(name) => name match {
        // TODO - ugly! must be a better way to deal with this
        case "Connected" => JsSuccess(Connected)
        case _ => JsError(s"unhandled event type: $name")
      }
      case _ => JsError("expecting string for event type")
    }

    override def writes(o: GameEventType): JsValue = JsString(o.toString)
  }

  implicit val gameEventFormat = Json.format[GameEvent]

  implicit val gameEventFrameFormatter = FrameFormatter.jsonFrame[GameEvent]

  implicit object GameFormat extends Format[Game] {
    override def reads(json: JsValue): JsResult[Game] = JsError("reads for game object not implemented")

    override def writes(o: Game): JsValue = {
      JsObject(Seq(
        "owner" -> JsString(o.owner),
        "numPlayers" -> JsNumber(o.numPlayers),
        "state" -> JsString(o.state.toString),
        "gameBoard" -> JsString("TODO"),
        "players" -> JsObject(o.playerHandles.mapValues(p => JsNumber(p.seat)).toSeq)
      ))
    }
  }

  implicit val gameFrameFormatter = FrameFormatter.jsonFrame[Game]
}