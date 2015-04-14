package controllers

import models.games.{PlayerHandle, WaitingForPlayers, Game, GameBoard}
import models.players.Player
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc._
import services.{PlayersManager, GamesManager}
import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

object Dominion extends Controller with DominionHelpers {
  def menu = registeredAction {
    def menuItem(game: Game) = GameMenuItem(game.owner, game.numPlayers, game.playerHandles.mapValues(_.player).values.toList, joinGameLinkGen)
    Action { implicit request =>
      sessionPlayerName
      val newGameLink = routes.Dominion.newGame().url
      val rejoinGames = sessionPlayerName.map(GamesManager.rejoinGames).getOrElse(TrieMap.empty)
        .map { case (_, game) => menuItem(game) }
        .seq
      val openGames = GamesManager.openGames
        .map { case (_, game) => menuItem(game) }
        .seq
      Ok(views.html.menu(newGameLink, rejoinGames, openGames))
    }
  }

  def newGame = registeredAction(Action(Ok(views.html.newGame(gameForm))))

  def createGame = registeredAction {
    Action { implicit request =>
      gameForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.newGame(formWithErrors)),
        newGame => {
          val board = GameBoard(newGame.numPlayers)
          val activePlayer = sessionPlayerName.get

          val game = Game(
            owner = activePlayer,
            numPlayers = newGame.numPlayers,
            state = WaitingForPlayers,
            gameBoard = board,
            playerHandles = TrieMap.empty[String, PlayerHandle]
          )

          GamesManager.putIfAbsent(game.owner, game)
          Redirect(routes.Dominion.joinGame(game.owner))
        }
      )
    }
  }

  def joinGame(owner: String) = registeredAction {
    Action { implicit request =>
      GamesManager.get(owner) match {
        case Some(game) => Ok(views.html.dominionBoard(game, sessionPlayerName.get))
        case None => NotFound // TODO - be nicer
      }
    }
  }

  def registerPlayer = Action { implicit request =>
    registerPlayerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.registerPlayer(formWithErrors)),
      registerPlayer => {
        val name = registerPlayer.name
        PlayersManager.putIfAbsent(name, Player(name, isConnected = true)) match {
          case Some(player) if player.isConnected => BadRequest(s"$name is already registered and current connected")
          case _ => Redirect(routes.Dominion.menu).withSession(PLAYER_NAME_KEY -> name)
        }
      }
    )
  }

  private def registeredAction[A](action: Action[A]) = Action.async(action.parser) { implicit request =>
    sessionPlayerName match {
      case Some(playerName) =>
        PlayersManager.get(playerName) match {
          case Some(player) => PlayersManager.update(playerName, player.copy(isConnected = true))
          case None => PlayersManager.putIfAbsent(playerName, Player(playerName, isConnected = true))
        }
        action(request)
      case None =>
        Future.successful(Ok(views.html.registerPlayer(registerPlayerForm)))
    }
  }
}

trait DominionHelpers {
  val PLAYER_NAME_KEY = "playerName"

  val gameForm = Form(
    mapping(
      "numPlayers" -> number.verifying(i => Range(2, 6).inclusive.contains(i))
    )(NewGame.apply)(NewGame.unapply)
  )

  val registerPlayerForm = Form(
    mapping(
      "name" -> text
    )(RegisterPlayer.apply)(RegisterPlayer.unapply)
  )

  def sessionPlayerName[A](implicit request: Request[A]): Option[String] = request.session.get(PLAYER_NAME_KEY)

  def joinGameLinkGen(owner: String): String = routes.Dominion.joinGame(owner).url
}

case class NewGame(numPlayers: Int)
case class GameMenuItem(owner: String, numPlayers: Int, joinedPlayers: List[Player], joinGameLinkGen: String => String)
case class RegisterPlayer(name: String)