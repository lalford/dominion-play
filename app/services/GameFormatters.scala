package services

import models.cards.Card
import models.games.{GameBoard, Game}
import models.players.PlayerHandle
import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

object GameFormatters {
  implicit object CardFormat extends Format[Card] {
    override def reads(json: JsValue): JsResult[Card] = JsError("reads for card object not implemented")

    override def writes(o: Card): JsValue = JsString(o.name)
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
        JsObject(Seq(
          "name" -> JsString(ph.player.name),
          "seat" -> JsNumber(ph.seat),
          "hand" -> cardsJson(ph.hand),
          "deck" -> cardsJson(ph.deck),
          "discard" -> cardsJson(ph.discard),
          "total" -> JsNumber(ph.total)
        ))
      })
    }

    private def cardsJson(cards: mutable.MutableList[Card]): JsValue = JsArray(cards.toSeq.map(Json.toJson(_)))
  }

  implicit val gameFrameFormatter = FrameFormatter.jsonFrame[Game]
}
