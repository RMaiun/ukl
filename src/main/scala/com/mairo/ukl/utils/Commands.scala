package com.mairo.ukl.utils

trait Commands {
  def commands():Seq[String]

  val ListPlayersCmd = "listPlayers"
  val AddPlayerCmd = "addPlayer"

  val AddRoundCmd = "addRound"
  val FindLastRoundsCmd = "findLastRounds"
  val LinkTidCmd = "linkTid"
  val ShortStatsCmd = "shortStats"
  val SubscribeCmd = "subscribe"
  val UnsubscribeCmd = "unsubscribe"
}
