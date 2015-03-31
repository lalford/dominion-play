package models.games.events

import play.api.libs.json.{Json, Reads}

case class Connect(gameOwner: String, player: String) extends GameEvent

object ConnectHandler extends GameEventHandler[Connect] {
  override def eventType: String = "Connect"

  override def reads: Reads[Connect] = Json.reads[Connect]
}