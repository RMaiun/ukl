package com.mairo.ukl.services

import cats.Monad
import com.mairo.ukl.dtos._
import com.mairo.ukl.repositories.RoundRepository
import com.mairo.ukl.services.impl.RoundServiceImpl
import com.mairo.ukl.utils.flow.Flow.Flow

trait RoundService[F[_]] {
  def findLastRoundsInSeason(dto: FindLastRoundsDto): Flow[F, FoundLastRoundsDto]

  def findAllRounds(seasonName: String): Flow[F, List[FullRound]]

  def saveRound(dto: AddRoundDto): Flow[F, IdDto]
}

object RoundService {
  def apply[F[_]](implicit ev: RoundService[F]): RoundService[F] = ev

  def impl[F[_] : Monad](PlayerService: PlayerService[F],
                         SeasonService: SeasonService[F],
                         RoundRepository: RoundRepository[F],
                         UserRightsService: UserRightsService[F]) =
    new RoundServiceImpl[F](PlayerService, SeasonService, RoundRepository, UserRightsService)
}