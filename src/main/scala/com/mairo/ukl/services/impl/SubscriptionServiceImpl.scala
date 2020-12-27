package com.mairo.ukl.services.impl

import java.time.{ZoneOffset, ZonedDateTime}

import cats.Monad
import com.mairo.ukl.dtos.{LinkTidDto, SubscriptionActionDto, SubscriptionResultDto}
import com.mairo.ukl.services.{PlayerService, SubscriptionService, UserRightsService}
import com.mairo.ukl.utils.flow.Flow.Flow
import com.mairo.ukl.validations.ValidationSet.{LinkTidValidator, SubscriptionActionValidator}
import com.mairo.ukl.validations.Validator

class SubscriptionServiceImpl[F[_] : Monad](userRightsService: UserRightsService[F],
                                            playerService: PlayerService[F]) extends SubscriptionService[F] {

  override def linkTidForPlayer(dto: LinkTidDto): Flow[F, SubscriptionResultDto] = {
    for {
      _ <- Validator.validateDto(dto)
      _ <- userRightsService.checkUserIsAdmin(dto.moderator)
      player <- playerService.enableNotifications(dto.nameToLink, dto.tid)
    } yield SubscriptionResultDto(dto.nameToLink, dto.tid, ZonedDateTime.now(ZoneOffset.UTC), player.notificationsEnabled)
  }

  override def updateSubscriptionsStatus(dto: SubscriptionActionDto): Flow[F, SubscriptionResultDto] = {
    for {
      _ <- Validator.validateDto(dto)
      foundPlayer <- playerService.findPlayerByTid(dto.tid)
      updPlayer <- playerService.updatePlayer(foundPlayer.copy(notificationsEnabled = dto.enableSubscriptions))
    } yield SubscriptionResultDto(updPlayer.surname, dto.tid, ZonedDateTime.now(ZoneOffset.UTC), updPlayer.notificationsEnabled)
  }
}
