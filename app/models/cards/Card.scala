package models.cards

import models.games.events.GameEvent

case class Card(name: String, cost: Int, traits: Set[CardTrait])

sealed trait CardTrait

case class Victory(points: Int) extends CardTrait
case class Treasure(value: Int) extends CardTrait
case class Action(events: List[GameEvent]) extends CardTrait
case class Duration(events: List[GameEvent]) extends CardTrait