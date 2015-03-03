package services

import models.games.Game
import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter

object GameFormatters {
  implicit object GameFormat extends Format[Game] {
    override def reads(json: JsValue): JsResult[Game] = JsError("reads for game object not implemented")

    override def writes(o: Game): JsValue = {
      JsObject(Seq(
        "owner" -> JsString(o.owner),
        "numPlayers" -> JsNumber(o.numPlayers),
        "state" -> JsString(o.state.toString),
        "gameBoard" -> JsString("TODO"),
        "players" -> JsObject(o.playerHandles.mapValues(p => JsNumber(p.seat)).toSeq)
      ))
    }
  }

  implicit val gameFrameFormatter = FrameFormatter.jsonFrame[Game]
}
