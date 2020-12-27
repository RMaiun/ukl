package com.mairo.ukl.services

import cats.Monad
import com.mairo.ukl.domains.Season
import com.mairo.ukl.errors.UklException.SeasonNotFoundException
import com.mairo.ukl.helper.QuarterCalculator
import com.mairo.ukl.repositories.SeasonRepository
import com.mairo.ukl.utils.flow.Flow.Flow
import com.mairo.ukl.utils.flow.Flow

trait SeasonService[F[_]] {
  def findSeasonByName(name: String): Flow[F, Season]

  def findSafe(season: String): Flow[F, Season]
}

object SeasonService {
  def apply[F[_]](implicit ev: SeasonService[F]): SeasonService[F] = ev

  def impl[F[_] : Monad](SR: SeasonRepository[F]): SeasonService[F] = new SeasonService[F] {
    override def findSeasonByName(name: String): Flow[F, Season] = {
      for {
        maybeSeason <- SR.getByName(name)
        result <- Flow.fromOption(maybeSeason, SeasonNotFoundException(name))
      } yield result
    }

    override def findSafe(season: String): Flow[F, Season] = {
      findSeasonByName(season).leftFlatMap(_ => storeSeasonIfNow(season))
    }

    private def storeSeasonIfNow(season: String): Flow[F, Season] = {
      val nowSeason = QuarterCalculator.currentQuarter
      if (nowSeason == season) {
        for {
          id <- SR.insert(nowSeason)
          maybeFound <- SR.getById(id)
          found <- Flow.fromOption(maybeFound, SeasonNotFoundException(season))
        } yield found
      } else {
        Flow.error(SeasonNotFoundException(season))
      }
    }
  }
}
