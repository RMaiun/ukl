package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class StreakDto(player: String, games: Int)

object StreakDto {
  implicit val streakDtoDecoder: Decoder[StreakDto] = deriveDecoder[StreakDto]

  implicit def streakDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, StreakDto] = jsonOf

  implicit val streakDtoEncoder: Encoder[StreakDto] = deriveEncoder[StreakDto]

  implicit def streakDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, StreakDto] = jsonEncoderOf

}