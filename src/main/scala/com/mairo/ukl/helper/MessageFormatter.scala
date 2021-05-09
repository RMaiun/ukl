package com.mairo.ukl.helper

import cats.Monad
import cats.syntax.either._
import com.mairo.ukl.dtos._
import com.mairo.ukl.utils.flow.Flow.Flow
import com.mairo.ukl.utils.flow.Flow

object MessageFormatter {

  def formatPlayerNotification[F[_] : Monad](opponent1: String, opponent2: String, isWinner: Boolean): Flow[F, String] = {
    val action = if (isWinner) "won" else "lost"
    Flow.pure(s"``` You $action the game against $opponent1/$opponent2```")
  }

  def formatPlayers[F[_] : Monad](data: FoundAllPlayersDto): Flow[F, String] = {
    val players = data.players.map(x => s"${x.id}|${x.surname.capitalize}").mkString("\n")
    val result =
      s"""```
         |$players```
           """.stripMargin
    Flow.pure(result)
  }

  def formatShortInfoStats[F[_] : Monad](data: SeasonShortStatsDto): Flow[F, String] = {
    data.gamesPlayed match {
      case 0 =>
        val msg =
          s"""```
             |No games found in season ${data.season}```
           """.stripMargin
            .asRight[Throwable]
        Flow.fromRes(msg)
      case _ =>
        val ratings = data.playersRating.indices.map(a => {
          val index = a + 1
          val name = data.playersRating(a).surname.capitalize
          val points = data.playersRating(a).score
          s"$index. $name $points"
        }).mkString(System.lineSeparator())
        val bestStreak = s"${data.bestStreak}: ${data.bestStreak.toString} games in row"
        val worstStreak = s"${data.worstStreak}: ${data.worstStreak.toString} games in row"
        val msg =
          s"""```
             |Season: ${data.season}
             |Games played: ${data.gamesPlayed}
             |Days till season end: ${data.daysToSeasonEnd}
             |${"-" * 30}
             |Current Rating:
             |$ratings
             |${"-" * 30}
             |Best Streak:
             |$bestStreak
             |${"-" * 30}
             |Worst Streak:
             |$worstStreak```
      """.stripMargin
        Flow.pure(msg)

    }
  }

  def formatLastRounds[F[_] : Monad](data: FoundLastRoundsDto): Flow[F, String] = {
    val result = data.rounds match {
      case Nil => "``` No data found```"
      case _ => formatLastRounds(data.rounds)
    }
    Flow.pure(result)
  }

  private def formatLastRounds(rounds: List[FullRound]): String = {
    val line = "-" * 34
    val formedBlocks = rounds.map(r =>
      s"""
         |date: ${DateFormatter.formatDate(r.created)}
         |winners: ${r.winner1}/${r.winner2}
         |losers: ${r.loser1}/${r.loser2}${if (r.shutout) s"${System.lineSeparator()}|shutout: ✓" else ""}
         |""".stripMargin
    ).mkString(line)
    s"``` $formedBlocks```"
  }

  def formatStoredRoundId[F[_] : Monad](data: IdDto): Flow[F, String] = {
    val msg =
      s"""```
         |New round was stored with id ${data.id}```
        """.stripMargin
        .asRight[Throwable]
    Flow.fromRes(msg)
  }

  def formatStoredPlayerId[F[_] : Monad](data: IdDto): Flow[F, String] = {
    val msg =
      s"""```
         |New players was stored with id ${data.id}```
        """.stripMargin
        .asRight[Throwable]
    Flow.fromRes(msg)
  }
}
