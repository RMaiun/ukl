package com.mairo.ukl.services

import cats.Monad
import com.mairo.ukl.dtos.{SeasonShortStatsDto, SeasonStatsRowsDto}
import com.mairo.ukl.helper.ConfigProvider.AppConfig
import com.mairo.ukl.services.impl.StatisticServiceImpl
import com.mairo.ukl.utils.Flow.Flow

trait StatisticService[F[_]] {
  def seasonStatisticsRows(seasonName: String): Flow[F, SeasonStatsRowsDto]

  def seasonShortInfoStatistics(seasonName: String): Flow[F, SeasonShortStatsDto]
}

object StatisticService {
  def apply[F[_]](implicit ev: StatisticService[F]): StatisticService[F] = ev

  def impl[F[_] : Monad](RoundService: RoundService[F], config: AppConfig): StatisticService[F] =
    new StatisticServiceImpl[F](RoundService, config)
}