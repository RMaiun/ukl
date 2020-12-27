package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class PlayerStatsDto(surname: String,
                          score: Int,
                          games: Int)

object PlayerStatsDto{
  implicit val playerStatsDtoDecoder: Decoder[PlayerStatsDto] = deriveDecoder[PlayerStatsDto]

  implicit def playerStatsDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, PlayerStatsDto] = jsonOf

  implicit val playerStatsDtoEncoder: Encoder[PlayerStatsDto] = deriveEncoder[PlayerStatsDto]

  implicit def playerStatsDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, PlayerStatsDto] = jsonEncoderOf
}
