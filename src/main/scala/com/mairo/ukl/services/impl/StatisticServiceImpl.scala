package com.mairo.ukl.services.impl

import cats.Monad
import com.mairo.ukl.dtos.InternalDto.{RatingWithGames, StatsCalcData}
import com.mairo.ukl.dtos.{FullRound, SeasonShortStatsDto, SeasonStatsRowsDto}
import com.mairo.ukl.helper.ConfigProvider.AppConfig
import com.mairo.ukl.services.{RoundService, StatisticService}
import com.mairo.ukl.utils.Flow.Flow

class StatisticServiceImpl[F[_] : Monad](RoundService: RoundService[F], config: AppConfig) extends StatisticService[F] {
  override def seasonStatisticsRows(seasonName: String): Flow[F, SeasonStatsRowsDto] = {
    RoundService.findAllRounds(seasonName)
  }

  override def seasonShortInfoStatistics(seasonName: String): Flow[F, SeasonShortStatsDto] = {

  }

  private def prepareSeasonStatsTable(rounds: List[FullRound]): SeasonStatsRowsDto = {

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
    dataByPlayer.map(x => ratingWithGames(x)).toList
  }

  private def ratingWithGames(e: (String, List[StatsCalcData])): RatingWithGames = {
    val id = e._2.headOption.fold(-1L)(x => x.pid)
    val points = e._2.map(_.points).sum + 1000
    val games = e._2.size
    RatingWithGames(id, e._1, points, games)
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
