package models.games.events

import play.api.libs.json.{Json, Reads}

case class Connected(gameOwner: String, player: String) extends GameEvent

object ConnectedHandler extends GameEventHandler[Connected] {
  override def eventType: String = "Connected"

  override def reads: Reads[Connected] = Json.reads[Connected]
}