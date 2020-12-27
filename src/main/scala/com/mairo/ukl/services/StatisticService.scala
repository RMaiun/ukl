package com.mairo.ukl.services

import cats.Monad
import com.mairo.ukl.dtos.{SeasonDto, SeasonShortStatsDto, SeasonStatsRowsDto}
import com.mairo.ukl.helper.ConfigProvider.AppConfig
import com.mairo.ukl.services.impl.StatisticServiceImpl
import com.mairo.ukl.utils.flow.Flow.Flow

trait StatisticService[F[_]] {
  def seasonStatisticsRows(season: SeasonDto): Flow[F, SeasonStatsRowsDto]

  def seasonShortInfoStatistics(season: SeasonDto): Flow[F, SeasonShortStatsDto]
}

object StatisticService {
  def apply[F[_]](implicit ev: StatisticService[F]): StatisticService[F] = ev

  def impl[F[_] : Monad](RoundService: RoundService[F], config: AppConfig): StatisticService[F] =
    new StatisticServiceImpl[F](RoundService, config)
}