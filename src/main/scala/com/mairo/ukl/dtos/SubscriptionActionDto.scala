package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class SubscriptionActionDto(enableSubscriptions:Boolean, tid: String)

object SubscriptionActionDto {
  implicit val SubscriptionActionDtoDecoder: Decoder[SubscriptionActionDto] = deriveDecoder[SubscriptionActionDto]

  implicit def SubscriptionActionDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, SubscriptionActionDto] = jsonOf

  implicit val SubscriptionActionDtoEncoder: Encoder[SubscriptionActionDto] = deriveEncoder[SubscriptionActionDto]

  implicit def SubscriptionActionDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, SubscriptionActionDto] = jsonEncoderOf

}
