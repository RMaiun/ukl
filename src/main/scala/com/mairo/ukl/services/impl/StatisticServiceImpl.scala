package com.mairo.ukl.services.impl

import java.time.LocalDate

import cats.Monad
import com.mairo.ukl.dtos.InternalDto.{RatingWithGames, StatsCalcData, StreakData}
import com.mairo.ukl.dtos._
import com.mairo.ukl.helper.ConfigProvider.AppConfig
import com.mairo.ukl.helper.{DateFormatter, SeasonHelper}
import com.mairo.ukl.services.{RoundService, StatisticService}
import com.mairo.ukl.utils.flow.Flow.Flow
import com.mairo.ukl.validations.ValidationSet.SeasonDtoValidator
import com.mairo.ukl.validations.Validator

import scala.annotation.tailrec

class StatisticServiceImpl[F[_] : Monad](roundService: RoundService[F],
                                         config: AppConfig) extends StatisticService[F] {
  type MutableStreakMap = collection.mutable.Map[String, StreakData]

  override def seasonStatisticsRows(season: SeasonDto): Flow[F, SeasonStatsRowsDto] = {
    roundService.findAllRounds(season.season).map(rounds => prepareSeasonStatsTable(rounds))
  }

  override def seasonShortInfoStatistics(season: SeasonDto): Flow[F, SeasonShortStatsDto] = {
    for {
      _ <- Validator.validateDto(season)
      rounds <- roundService.findAllRounds(season.season)
    } yield prepareSeasonShortStats(season.season, rounds)
  }

  private def prepareSeasonShortStats(seasonName: String, rounds: List[FullRound]): SeasonShortStatsDto = {
    val seasonGate = SeasonHelper.seasonGate(seasonName)
    val now = LocalDate.now()
    val daysTillSeasonEnd = calculateDaysToSeasonEnd(now, seasonGate)

    val topPlayers = calculatePointsForPlayers(rounds, shortStats = true)
      .sortBy(-_.rating)
      .map(rwg => PlayerStatsDto(rwg.player, rwg.rating, rwg.games))

    val topPair = calculateStreaks(rounds)
    SeasonShortStatsDto(seasonName, topPlayers, rounds.size, daysTillSeasonEnd, Some(topPair._1), Some(topPair._2))
  }

  private def calculateStreaks(rounds: List[FullRound]): (StreakDto, StreakDto) = {
    val results = rounds.flatMap(r => List(r.winner1, r.winner2, r.loser1, r.loser2))
      .distinct
      .map(x => (x, StreakData()))
      .toMap
    val sortedRounds = rounds.sortBy(_.created)

    @tailrec
    def mergeResults(results: Map[String, StreakData], roundList: List[FullRound]): Map[String, StreakData] = {
      if (roundList.isEmpty) {
        results
      } else {
        val round = roundList.head
        val w1Map = prepareOnePlayerMap(round.winner1, 1, results)
        val w2Map = prepareOnePlayerMap(round.winner2, 1, w1Map)
        val l1Map = prepareOnePlayerMap(round.loser1, -1, w2Map)
        val l2Map = prepareOnePlayerMap(round.loser2, -1, l1Map)
        mergeResults(l2Map, roundList.tail)
      }
    }

    val generatedResults = mergeResults(results, sortedRounds)
    val best = generatedResults.toList.map(p => StreakDto(p._1, p._2.maxWin)).maxBy(_.games)
    val worst = generatedResults.toList.map(p => StreakDto(p._1, p._2.maxLose)).maxBy(_.games)
    (best, worst)
  }

  private def updatedMap(map: Map[String, StreakData], key: String, value: StreakData): Map[String, StreakData] =
    map + ((key, value))

  private def prepareOnePlayerMap(player: String, score: Int, results: Map[String, StreakData]): Map[String, StreakData] = {
    val found = results.getOrElse(player, StreakData())
    if (score > 0) {
      val currentWin = found.currentWin + 1
      val maxWin = if (currentWin > found.maxWin) currentWin else found.maxWin
      updatedMap(results, player, StreakData(currentWin, maxWin, 0, found.maxLose))
    } else {
      val currentLose: Int = found.currentLose + 1
      val maxLose: Int = if (currentLose > found.maxLose) currentLose else found.maxLose
      updatedMap(results, player, StreakData(0, found.maxWin, currentLose, maxLose))
    }
  }

  private def calculateDaysToSeasonEnd(now: LocalDate, seasonGate: (LocalDate, LocalDate)): Int = {
    if (now.compareTo(seasonGate._2) > 0) 0
    else seasonGate._2.getDayOfYear - now.getDayOfYear
  }

  private def prepareSeasonStatsTable(rounds: List[FullRound]): SeasonStatsRowsDto = {
    val playerStats = calculatePointsForPlayers(rounds, shortStats = false)
    val headers = playerStats.sortBy(_.pid).map(_.player)
    val totals = playerStats.sortBy(_.pid).map(_.rating)
    val games = rounds.map(r => transformRoundIntoRow(r, headers))
    val createDates = rounds.map(_.created).map(DateFormatter.formatDateWithHour)
    SeasonStatsRowsDto(headers, totals, games, createDates, rounds.size)
  }

  private def transformRoundIntoRow(r: FullRound, headers: List[String]): List[String] = {
    val winP: String = s"${winPoints(r.shutout)}"
    val loseP: String = s"${losePoints(r.shutout)}"
    headers.map {
      case x if x == r.winner1 => winP
      case x if x == r.winner2 => winP
      case x if x == r.loser1 => loseP
      case x if x == r.loser2 => loseP
      case _ => ""
    }
  }

  private def calculatePointsForPlayers(rounds: List[FullRound], shortStats: Boolean): List[RatingWithGames] = {

    val roundData = rounds.flatMap(r => List(
      StatsCalcData(r.w1Id, r.winner1, winPoints(r.shutout)),
      StatsCalcData(r.w2Id, r.winner2, winPoints(r.shutout)),
      StatsCalcData(r.l1Id, r.loser1, losePoints(r.shutout)),
      StatsCalcData(r.l2Id, r.loser2, losePoints(r.shutout)),
    ))
    val acceptedPlayers = prepareAcceptedPlayersForShortStats(roundData, shortStats)
    val dataByPlayer = roundData.filter(x => acceptedPlayers.contains(x.player)).groupBy(_.player)
    prepareRatingWithGames(dataByPlayer)
  }

  private def prepareAcceptedPlayersForShortStats(roundData: List[StatsCalcData], filterByGames: Boolean): Map[String, Int] = {
    val data = roundData.groupBy(_.player).view.mapValues(list => list.map(_.qty).sum)
    if (filterByGames) {
      data.filter(p => p._2 >= 30).toMap
    } else {
      data.toMap
    }
  }

  private def prepareRatingWithGames(dataByPlayer: Map[String, List[StatsCalcData]]): List[RatingWithGames] = {
    dataByPlayer.toList.map(x => ratingWithGames(x._1, x._2))
  }

  private def ratingWithGames(player: String, data: List[StatsCalcData]): RatingWithGames = {
    val id = data.headOption.fold(-1L)(x => x.pid)
    val points = data.map(_.points).sum + 1000
    val games = data.size
    RatingWithGames(id, player, points, games)
  }

  private def winPoints(shutout: Boolean): Int = {
    calculatePoints(win = true, shutout = shutout)
  }

  private def losePoints(shutout: Boolean): Int = {
    calculatePoints(win = false, shutout = shutout)
  }

  private def calculatePoints(win: Boolean, shutout: Boolean): Int = {
    (win, shutout) match {
      case (true, true) => config.winShutoutPoints
      case (true, false) => config.winPoints
      case (false, false) => config.losePoints
      case (false, true) => config.loseShutoutPoints
    }
  }


}
