package services

import models.cards.Card
import models.games.{GameBoard, PlayerHandle, Game}
import models.players.Player
import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

object GameFormatters {
  implicit object CardFormat extends Format[Card] {
    override def reads(json: JsValue): JsResult[Card] = JsError("reads for card object not implemented")

    override def writes(o: Card): JsValue = JsString(o.name)
  }

  implicit object PlayerFormat extends Format[Player] {
    override def reads(json: JsValue): JsResult[Player] = JsError("reads for player object not implemented")

    override def writes(o: Player): JsValue = {
      JsObject(Seq(
        "name" -> JsString(o.name),
        "hand" -> cardsJson(o.hand),
        "deck" -> cardsJson(o.deck),
        "discard" -> cardsJson(o.discard),
        "total" -> JsNumber(o.total)
      ))
    }

    private def cardsJson(cards: mutable.MutableList[Card]): JsValue = JsArray(cards.toSeq.map(Json.toJson(_)))
  }

  implicit object GameFormat extends Format[Game] {
    override def reads(json: JsValue): JsResult[Game] = JsError("reads for game object not implemented")

    override def writes(o: Game): JsValue = {
      JsObject(Seq(
        "owner" -> JsString(o.owner),
        "numPlayers" -> JsNumber(o.numPlayers),
        "state" -> JsString(o.state.toString),
        "gameBoard" -> gameBoardJson(o.gameBoard),
        "players" -> playerHandlesJson(o.playerHandles)
      ))
    }

    private def gameBoardJson(board: GameBoard): JsValue = {
      JsObject(Seq(
        "victoryBoard" -> board.victoryBoard.jsValue,
        "treasureBoard" -> board.treasureBoard.jsValue,
        "kingdomBoard" -> board.kingdomBoard.jsValue,
        "trash" -> JsArray(board.trash.map(card => JsString(card.name)))
      ))
    }

    private def playerHandlesJson(playerHandles: TrieMap[String, PlayerHandle]): JsValue = {
      JsArray(playerHandles.toSeq.map { case (name, ph) =>
        Json.toJson(ph.player).asInstanceOf[JsObject] + ("seat" -> JsNumber(ph.seat))
      })
    }
  }

  implicit val gameFrameFormatter = FrameFormatter.jsonFrame[Game]
}
