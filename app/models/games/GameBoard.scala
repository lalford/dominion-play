package models.games

import models.cards.Card
import play.api.libs.json.{JsString, JsNumber, JsObject, JsValue}

case class GameBoard(
  victoryBoard: VictoryBoard,
  treasureBoard: TreasureBoard,
  kingdomBoard: KingdomBoard,
  trash: List[Card] = List()
)

object GameBoard {
  def apply(numPlayers: Int): GameBoard = {
    val victoryBoard = VictoryBoard(numPlayers)
    val treasureBoard = TreasureBoard(numPlayers)
    val kingdomBoard = KingdomBoard()

    GameBoard(victoryBoard, treasureBoard, kingdomBoard)
  }
}

case class VictoryBoard(estate: Deck, duchy: Deck, province: Deck, colony: Option[Deck], curse: Option[Deck]) {
  def jsValue: JsValue = {
    val required = JsObject(Seq(
      estate.entry,
      duchy.entry,
      province.entry
    ))

    val withColony = colony.map(deck => required + deck.entry).getOrElse(required)

    curse.map(deck => withColony + deck.entry).getOrElse(withColony)
  }
}

object VictoryBoard {
  def apply(numPlayers: Int): VictoryBoard = numPlayers match {
    case 2 => makeBoard(victoryCount = 8, colonyCount = Some(8), curseCount = Some(20))
    case 3 => makeBoard(victoryCount = 12, colonyCount = Some(12), curseCount = Some(30))
    case 4 => makeBoard(victoryCount = 12, colonyCount = Some(12), curseCount = Some(30))
    case 5 => makeBoard(victoryCount = 12, colonyCount = Some(12), curseCount = Some(40), extraProvinces = 3)
    case _ => makeBoard(victoryCount = 12, colonyCount = Some(12), curseCount = Some(50), extraProvinces = 6)
  }

  private def makeBoard(victoryCount: Int, extraProvinces: Int = 0, colonyCount: Option[Int] = None, curseCount: Option[Int] = None): VictoryBoard = {
    val estate = Deck("Estate", 2, victoryCount)
    val duchy = Deck("Duchy", 5, victoryCount)
    val province = Deck("Province", 8, victoryCount + extraProvinces)
    val colony = colonyCount.map(Deck("Colony", 11, _))
    val curse = curseCount.map(Deck("Curse", 0, _))
    VictoryBoard(estate, duchy, province, colony, curse)
  }
}

case class TreasureBoard(copper: Deck, silver: Deck, gold: Deck, platinum: Option[Deck]) {
  def jsValue: JsValue = {
    val required = JsObject(Seq(
      copper.entry,
      silver.entry,
      gold.entry
    ))

    platinum.map(deck => required + deck.entry).getOrElse(required)
  }
}

object TreasureBoard {
  def apply(numPlayers: Int): TreasureBoard = numPlayers match {
    case 2 => makeBoard(copperCount = 60, silverCount = 40, goldCount = 30, platinumCount = Some(12))
    case 3 => makeBoard(copperCount = 60, silverCount = 40, goldCount = 30, platinumCount = Some(12))
    case 4 => makeBoard(copperCount = 60, silverCount = 40, goldCount = 30, platinumCount = Some(12))
    case 5 => makeBoard(copperCount = 120, silverCount = 80, goldCount = 60, platinumCount = Some(12))
    case _ => makeBoard(copperCount = 120, silverCount = 80, goldCount = 60, platinumCount = Some(12))
  }

  private def makeBoard(copperCount: Int, silverCount: Int, goldCount: Int, platinumCount: Option[Int] = None): TreasureBoard = {
    val copper = Deck("Copper", 0, copperCount)
    val silver = Deck("Silver", 3, silverCount)
    val gold = Deck("Gold", 6, goldCount)
    val platinum = platinumCount.map(Deck("Platinum", 9, _))
    TreasureBoard(copper, silver, gold, platinum)
  }
}

case class KingdomBoard(kingdoms: List[Deck] = Nil) {
  def jsValue: JsValue = JsObject(kingdoms.map(_.entry))
}

case class Deck(card: String, cost: Int, quantity: Int) {
  def entry: (String, JsValue) = card -> JsObject(Seq(
    "card" -> JsString(card),
    "cost" -> JsNumber(cost),
    "quantity" -> JsNumber(quantity)
  ))
}