package com.mairo.ukl.errors

object UklException {

  case class DbException(cause:Throwable) extends RuntimeException(cause)
  case class InvalidUserRightsException() extends RuntimeException("Not enough rights to persist data.")
  case class PlayersNotFoundException(players: List[String]) extends RuntimeException(s"Players with names: [${players.mkString(",")}] were not found.")
  case class PlayerAlreadyExistsException(uid:Long) extends RuntimeException(s"Player with given name already exists with id $uid.")
  case class SeasonNotFoundException(season:String) extends RuntimeException(s"Season $season is not found.")
  case class SamePlayersInRoundException() extends RuntimeException("All players in round must be different")
}