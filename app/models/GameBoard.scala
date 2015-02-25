package models

case class GameBoard(
  players: List[Player],
  victoryBoard: VictoryBoard,
  treasureBoard: TreasureBoard,
  kingdomBoard: KingdomBoard,
  trash: Set[Card] = Set()
)

object GameBoard {
  def apply(numPlayers: Int): GameBoard = {
    val victorySet = VictoryBoard(numPlayers)
    val treasureSet = TreasureBoard(numPlayers)
    val kingdomSet = KingdomBoard()

    val players = List()

    GameBoard(players, victorySet, treasureSet, kingdomSet)
  }
}

case class VictoryBoard(
  curses: List[Curse],
  estates: List[Estate],
  duchies: List[Duchy],
  provinces: List[Province]
)

object VictoryBoard {
  def apply(numPlayers: Int): VictoryBoard = numPlayers match {
    case 2 => makeBoard(victoryCount = 8, curseCount = 20)
    case 3 => makeBoard(victoryCount = 12, curseCount = 30)
    case 4 => makeBoard(victoryCount = 12, curseCount = 30)
    case 5 => makeBoard(victoryCount = 12, curseCount = 40, extraProvinces = 3)
    case _ => makeBoard(victoryCount = 12, curseCount = 50, extraProvinces = 6)
  }

  private def makeBoard(victoryCount: Int, extraProvinces: Int = 0, curseCount: Int): VictoryBoard = {
    val countList = (1 to victoryCount).toList
    val estates = countList.map(_ => Estate())
    val duchies = countList.map(_ => Duchy())
    val provinces = countList.map(_ => Province()) ::: (1 to extraProvinces).toList.map(_ => Province())
    val curses = (1 to curseCount).toList.map(_ => Curse())
    VictoryBoard(curses, estates, duchies, provinces)
  }
}

case class TreasureBoard(
  coppers: List[Copper],
  silvers: List[Silver],
  golds: List[Gold]
)

object TreasureBoard {
  def apply(numPlayers: Int): TreasureBoard = numPlayers match {
    case 2 => makeBoard(copperCount = 60, silverCount = 40, goldCount = 30)
    case 3 => makeBoard(copperCount = 60, silverCount = 40, goldCount = 30)
    case 4 => makeBoard(copperCount = 60, silverCount = 40, goldCount = 30)
    case 5 => makeBoard(copperCount = 120, silverCount = 80, goldCount = 60)
    case _ => makeBoard(copperCount = 120, silverCount = 80, goldCount = 60)
  }

  private def makeBoard(copperCount: Int, silverCount: Int, goldCount: Int): TreasureBoard = {
    val coppers = (1 to copperCount).toList.map(_ => Copper())
    val silvers = (1 to silverCount).toList.map(_ => Silver())
    val golds = (1 to goldCount).toList.map(_ => Gold())
    TreasureBoard(coppers, silvers, golds)
  }
}

case class KingdomBoard(kingdoms: List[List[Card]])

object KingdomBoard {
  def apply(): KingdomBoard = {
    val kingdoms = (1 to 10).toList map { i =>
      val name = s"Ex $i"
      val cost = i
      val traits: Set[CardTrait] = {
        if (i % 3 == 0) Set(Action(List()), Duration(List()))
        else Set(Action(List()))
      }
      (1 to 10).toList.map(_ => Kingdom(name, cost, traits))
    }
    KingdomBoard(kingdoms)
  }
}