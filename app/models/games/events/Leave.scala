package models.games.events

import play.api.libs.json.{Json, Reads}

case class Leave(gameOwner: String, player: String) extends GameEvent

object LeaveHandler extends GameEventHandler[Leave] {
  override def eventType: String = "Leave"

  override def reads: Reads[Leave] = Json.reads[Leave]
}
