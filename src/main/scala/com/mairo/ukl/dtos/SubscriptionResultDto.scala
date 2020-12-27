package com.mairo.ukl.dtos

import java.time.ZonedDateTime

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class SubscriptionResultDto(subscribedSurname: String,
                                 subscribedTid: String,
                                 createdTime: ZonedDateTime,
                                 notificationsEnabled: Boolean)

object SubscriptionResultDto {
  implicit val SubscriptionResultDtoDecoder: Decoder[SubscriptionResultDto] = deriveDecoder[SubscriptionResultDto]

  implicit def SubscriptionResultDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, SubscriptionResultDto] = jsonOf

  implicit val SubscriptionResultDtoEncoder: Encoder[SubscriptionResultDto] = deriveEncoder[SubscriptionResultDto]

  implicit def aSubscriptionResultDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, SubscriptionResultDto] = jsonEncoderOf

}
