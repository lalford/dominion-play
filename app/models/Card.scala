package models

class Event()

sealed trait CardTrait
case class Victory(points: Int) extends CardTrait
case class Treasure(value: Int) extends CardTrait
case class Action(events: List[Event]) extends CardTrait
case class Duration(events: List[Event]) extends CardTrait

sealed trait Card {
  def name: String
  def cost: Int
  def traits: Set[CardTrait]
}

// card list
case class Curse() extends Card {
  def name: String = "Curse"
  def cost: Int = 0
  def traits: Set[CardTrait] = Set(Victory(points = -1))
}

case class Estate() extends Card {
  def name: String = "Estate"
  def cost: Int = 2
  def traits: Set[CardTrait] = Set(Victory(points = 1))
}

case class Duchy() extends Card {
  def name: String = "Duchy"
  def cost: Int = 5
  def traits: Set[CardTrait] = Set(Victory(points = 3))
}

case class Province() extends Card {
  def name: String = "Province"
  def cost: Int = 8
  def traits: Set[CardTrait] = Set(Victory(points = 6))
}

case class Copper() extends Card {
  def name: String = "Copper"
  def cost: Int = 0
  def traits: Set[CardTrait] = Set(Treasure(value = 1))
}

case class Silver() extends Card {
  def name: String = "Silver"
  def cost: Int = 3
  def traits: Set[CardTrait] = Set(Treasure(value = 2))
}

case class Gold() extends Card {
  def name: String = "Gold"
  def cost: Int = 6
  def traits: Set[CardTrait] = Set(Treasure(value = 3))
}

case class Kingdom(name: String, cost: Int, traits: Set[CardTrait]) extends Card