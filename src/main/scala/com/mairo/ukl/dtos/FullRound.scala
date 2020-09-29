package com.mairo.ukl.dtos

import java.time.LocalDateTime

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class FullRound(w1Id: Long,
                     winner1: String,
                     w2Id: Long,
                     winner2: String,
                     l1Id: Long,
                     loser1: String,
                     l2Id: Long,
                     loser2: String,
                     created: LocalDateTime,
                     season: String,
                     shutout: Boolean)

object FullRound {
  implicit val fullRoundDtoDecoder: Decoder[FullRound] = deriveDecoder[FullRound]

  implicit def fullRoundDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, FullRound] = jsonOf

  implicit val fullRoundDtoEncoder: Encoder[FullRound] = deriveEncoder[FullRound]

  implicit def fullRoundDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, FullRound] = jsonEncoderOf
}
