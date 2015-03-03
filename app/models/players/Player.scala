package models.players

import models.cards.Card

import scala.collection.mutable.MutableList

case class Player(
  name: String,
  isConnected: Boolean,
  hand: MutableList[Card] = MutableList(),
  deck: MutableList[Card] = MutableList(),
  discard: MutableList[Card] = MutableList()
)
