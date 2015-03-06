package models.games

import models.cards.Card
import play.api.libs.json.{JsNumber, JsObject, JsValue}

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

case class VictoryBoard(estate: Deck, duchy: Deck, province: Deck, curse: Option[Deck]) {
  def jsValue: JsValue = {
    val required = JsObject(Seq(
      estate.card -> JsNumber(estate.quantity),
      duchy.card -> JsNumber(duchy.quantity),
      province.card -> JsNumber(province.quantity)
    ))

    curse.map(deck => required + (deck.card -> JsNumber(deck.quantity))).getOrElse(required)
  }
}

object VictoryBoard {
  def apply(numPlayers: Int): VictoryBoard = numPlayers match {
    case 2 => makeBoard(victoryCount = 8, curseCount = Some(20))
    case 3 => makeBoard(victoryCount = 12, curseCount = Some(30))
    case 4 => makeBoard(victoryCount = 12, curseCount = Some(30))
    case 5 => makeBoard(victoryCount = 12, curseCount = Some(40), extraProvinces = 3)
    case _ => makeBoard(victoryCount = 12, curseCount = Some(50), extraProvinces = 6)
  }

  private def makeBoard(victoryCount: Int, extraProvinces: Int = 0, curseCount: Option[Int] = None): VictoryBoard = {
    val estate = Deck("Estate", victoryCount)
    val duchy = Deck("Duchy", victoryCount)
    val province = Deck("Province", victoryCount + extraProvinces)
    val curse = curseCount.map(Deck("Curse", _))
    VictoryBoard(estate, duchy, province, curse)
  }
}

case class TreasureBoard(copper: Deck, silver: Deck, gold: Deck, platinum: Option[Deck]) {
  def jsValue: JsValue = {
    val required = JsObject(Seq(
      copper.card -> JsNumber(copper.quantity),
      silver.card -> JsNumber(silver.quantity),
      gold.card -> JsNumber(gold.quantity)
    ))

    platinum.map(deck => required + (deck.card -> JsNumber(deck.quantity))).getOrElse(required)
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
    val copper = Deck("Copper", copperCount)
    val silver = Deck("Silver", silverCount)
    val gold = Deck("Gold", goldCount)
    val platinum = platinumCount.map(Deck("Platinum", _))
    TreasureBoard(copper, silver, gold, platinum)
  }
}

case class KingdomBoard(kingdoms: List[Deck] = Nil) {
  def jsValue: JsValue = JsObject(kingdoms.map(deck => deck.card -> JsNumber(deck.quantity)))
}

case class Deck(card: String, quantity: Int)