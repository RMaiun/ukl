package com.mairo.ukl.services

import cats.Monad
import com.mairo.ukl.dtos.{LinkTidDto, SubscriptionActionDto, SubscriptionResultDto}
import com.mairo.ukl.services.impl.SubscriptionServiceImpl
import com.mairo.ukl.utils.flow.Flow.Flow

trait SubscriptionService[F[_]] {
  def linkTidForPlayer(dto: LinkTidDto): Flow[F, SubscriptionResultDto]

  def updateSubscriptionsStatus(dto: SubscriptionActionDto): Flow[F, SubscriptionResultDto]
}

object SubscriptionService {
  def apply[F[_]](implicit ev: SubscriptionService[F]): SubscriptionService[F] = ev

  def impl[F[_] : Monad](userRightsService: UserRightsService[F],
                         playerService: PlayerService[F]): SubscriptionService[F] =
    new SubscriptionServiceImpl[F](userRightsService, playerService)
}