package controllers

import akka.actor.{PoisonPill, Actor, Props, ActorRef}
import models.games.Game
import models.games.events._
import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, OWrites, OFormat, JsValue}
import play.api.mvc.{WebSocket, Controller}
import play.api.Play.current
import services.{GameEventHandlers, GamesManager}

object GameSocket extends Controller {
  import services.GameFormatters._

  GameEventHandlers.register(ConnectHandler)
  GameEventHandlers.register(NewKingdomBoardHandler)

  def socket = WebSocket.acceptWithActor[JsValue, Game] { request => out =>
    GameSocketActor.props(out)
  }
}

object GameSocketActor {
  def props(out: ActorRef) = Props(new GameSocketActor(out))
}

class GameSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case json: JsValue =>
      val eventType = (json \ "eventType").as[String]
      val eventHandler = GameEventHandlers.handlerFor(eventType)
      gameEventFormat(eventHandler)
        .reads(json)
        .fold(handleBadEvent, handleGameEvent(eventType, _))
    case _ =>
  }

  private def gameEventFormat[A <: GameEvent](handler: GameEventHandler[A]) = {
    OFormat[A](
      handler.reads,
      OWrites[A](_ => throw new UnsupportedOperationException("game events are read only"))
    )
  }

  private def handleBadEvent(errors: Seq[(JsPath, Seq[ValidationError])]): Unit = {
    val formattedErrors = errors.map { case (path, pathErrors) =>
      val formattedPathErrors = pathErrors.map(e => s"  ${e.message}").mkString("\n")
      s"${path.toString()}\n$formattedPathErrors"
    }
    Logger.error(s"bad game event: $formattedErrors")
  }

  private def handleGameEvent(eventType: String, event: GameEvent): Unit = {
    event match {
      case c: Connect =>
        GamesManager.addPlayerHandle(c.gameOwner, c.player, out)
      case kb: NewKingdomBoard =>
        GamesManager.addNewKingdomBoard(kb.gameOwner, kb.kingdomBoard)
      case _ =>
        Logger.error(s"closing socket due to unhandled event type: $eventType")
        self ! PoisonPill
    }

    GamesManager.gameBroadcast(event.gameOwner)
  }
}