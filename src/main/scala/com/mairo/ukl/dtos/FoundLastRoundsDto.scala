package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class FoundLastRoundsDto(rounds: List[FullRound])

object FoundLastRoundsDto {
  implicit val foundLastRoundsDtoDecoder: Decoder[FoundLastRoundsDto] = deriveDecoder[FoundLastRoundsDto]

  implicit def foundLastRoundsDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, FoundLastRoundsDto] = jsonOf

  implicit val foundLastRoundsDtoEncoder: Encoder[FoundLastRoundsDto] = deriveEncoder[FoundLastRoundsDto]

  implicit def foundLastRoundsDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, FoundLastRoundsDto] = jsonEncoderOf
}