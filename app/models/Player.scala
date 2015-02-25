package models

import collection.mutable.MutableList

case class Player(
  name: String,
  isConnected: Boolean,
  hand: MutableList[Card] = MutableList(),
  deck: MutableList[Card] = MutableList(),
  discard: MutableList[Card] = MutableList()
)
