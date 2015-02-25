package controllers

import akka.actor.{Actor, Props, ActorRef}
import models.{GameEventSerializers, Connected, GameEvent}
import play.api.mvc.{WebSocket, Controller}
import play.api.Play.current
import services.GamesManager

object GameSocket extends Controller {
  import GameEventSerializers._

  def socket = WebSocket.acceptWithActor[Connected, String] { request => out =>
    GameSocketActor.props(out)
  }
}

object GameSocketActor {
  def props(out: ActorRef) = Props(new GameSocketActor(out))
}

class GameSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case connected: Connected =>
      GamesManager.addPlayerHandle(connected.owner, connected.player, out)
      out ! s"Got your request to connect ${connected.player} to ${connected.owner}, got to figure out data for rebuilding game state UI"
      GamesManager.gameBroadcast(connected.owner, s"${connected.player} has joined")
      // TODO - check if we've reached critical mass, update game state
  }
}