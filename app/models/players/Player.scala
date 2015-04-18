package models.players

import akka.actor.ActorRef
import models.cards.Card

import scala.collection.mutable.MutableList

case class Player(name: String)

case class PlayerHandle(
  gameSocket: Option[ActorRef],
  seat: Int,
  player: Player,
  hand: MutableList[Card] = MutableList(),
  deck: MutableList[Card] = MutableList(),
  discard: MutableList[Card] = MutableList()
) {
  def total: Int = hand.length + deck.length + discard.length
}