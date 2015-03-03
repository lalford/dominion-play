package models.games.events

import play.api.libs.json.Reads

trait GameEvent {
  def gameOwner: String
  def player: String
}

trait GameEventHandler[A <: GameEvent] {
  def eventType: String
  def reads: Reads[A]
}