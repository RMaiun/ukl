package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class FoundLastRounds(rounds: List[FullRound])

object FoundLastRounds {
  implicit val foundLastRoundsDtoDecoder: Decoder[FoundLastRounds] = deriveDecoder[FoundLastRounds]

  implicit def foundLastRoundsDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, FoundLastRounds] = jsonOf

  implicit val foundLastRoundsDtoEncoder: Encoder[FoundLastRounds] = deriveEncoder[FoundLastRounds]

  implicit def foundLastRoundsDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, FoundLastRounds] = jsonEncoderOf
}