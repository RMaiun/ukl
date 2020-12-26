package com.mairo.ukl.errors

import io.circe.Json

object UklException {

  case class DbException(cause: Throwable) extends RuntimeException(cause)

  case class ValidationException(errors: Set[String]) extends RuntimeException(s"ValidationErrors: ${errors.mkString("[", ";", "]")}")

  case class InvalidUserRightsException() extends RuntimeException("Not enough rights to persist data.")

  case class PlayersNotFoundException(players: List[String]) extends RuntimeException(s"Players with names: [${players.mkString(",")}] were not found.")

  case class PlayerNotFoundException(criteria:String, value: String) extends RuntimeException(s"Player with $criteria: $value was not found.")

  case class PlayerAlreadyExistsException(uid: Long) extends RuntimeException(s"Player with given name already exists with id $uid.")

  case class SeasonNotFoundException(season: String) extends RuntimeException(s"Season $season is not found.")

  case class SamePlayersInRoundException() extends RuntimeException("All players in round must be different")

  case class InvalidTelegramCmd(cmd: String) extends RuntimeException(s"Command $cmd is not currently supported")

  case class InvalidBotRequest(chatId: Option[String], cmd: Option[String], body: Option[Json] = None)
    extends RuntimeException(s"Some of required params [cmd, chatId, requestBody] are missed [$cmd, $chatId, $body]")

  case class AbsentCommandInput() extends RuntimeException(s"Command was recognized without input")

  case class InvalidCommandException(cmd: String) extends RuntimeException(s"Command $cmd is not supported.")

}