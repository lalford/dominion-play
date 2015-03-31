package models.games.events

import models.games.{Deck, KingdomBoard}
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class NewKingdomBoard(gameOwner: String, player: String, kingdomBoard: KingdomBoard) extends GameEvent

object NewKingdomBoardHandler extends GameEventHandler[NewKingdomBoard] {
  override def eventType: String = "New Kingdom Board"

  implicit def readsDeck: Reads[Deck] = Json.reads[Deck]

  implicit def readsKingdomBoard: Reads[KingdomBoard] = Json.reads[KingdomBoard]

  override def reads: Reads[NewKingdomBoard] = (
    (__ \ 'gameOwner).read[String] and
    (__ \ 'player).read[String] and
    (__ \ 'kingdomBoard).read[List[Deck]].fmap(KingdomBoard)
  )(NewKingdomBoard.apply _)
}
