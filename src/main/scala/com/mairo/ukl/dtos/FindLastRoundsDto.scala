package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class FindLastRoundsDto(season: String, qty: Int)

object FindLastRoundsDto {
  implicit val findLastRoundsDtoDecoder: Decoder[FindLastRoundsDto] = deriveDecoder[FindLastRoundsDto]

  implicit def findLastRoundsDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, FindLastRoundsDto] = jsonOf

  implicit val findLastRoundsDtoEncoder: Encoder[FindLastRoundsDto] = deriveEncoder[FindLastRoundsDto]

  implicit def findLastRoundsDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, FindLastRoundsDto] = jsonEncoderOf
}
