package controllers

import akka.actor.{PoisonPill, Actor, Props, ActorRef}
import models.{Connected, Game, GameSerializers, GameEvent}
import play.api.Logger
import play.api.mvc.{WebSocket, Controller}
import play.api.Play.current
import services.GamesManager

object GameSocket extends Controller {
  import GameSerializers._

  def socket = WebSocket.acceptWithActor[GameEvent, Game] { request => out =>
    GameSocketActor.props(out)
  }
}

object GameSocketActor {
  def props(out: ActorRef) = Props(new GameSocketActor(out))
}

class GameSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case gameEvent: GameEvent =>
      val gameOwner = gameEvent.gameOwner
      val player = gameEvent.player

      gameEvent.eventType match {
        case Connected =>
          GamesManager.addPlayerHandle(gameOwner, player, out)
        case _ =>
          Logger.error(s"closing socket due to unhandled event type: ${gameEvent.eventType}")
          self ! PoisonPill
      }

      GamesManager.gameBroadcast(gameOwner)
  }
}