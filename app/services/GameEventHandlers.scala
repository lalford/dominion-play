package services

import models.games.events.{GameEvent, GameEventHandler}

import scala.collection.concurrent.TrieMap

object GameEventHandlers {
  private val handlers = TrieMap.empty[String, HandlerEntry[_ <: GameEvent]]

  def register[A <: GameEvent](handler: GameEventHandler[A]): this.type = {
    handlers.put(handler.eventType, HandlerEntry(handler))
    this
  }

  def handlerFor(eventType: String): GameEventHandler[_ <: GameEvent] = {
    handlers
      .get(eventType)
      .map(_.handler)
      .getOrElse(throw new RuntimeException(s"no handler registered for event type $eventType"))
  }
}

private[this] case class HandlerEntry[A <: GameEvent](handler: GameEventHandler[A])