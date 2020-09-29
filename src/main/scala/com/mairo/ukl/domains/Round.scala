package com.mairo.ukl.domains

import java.time.LocalDateTime

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class Round(id: Long,
                 winner1: Long,
                 winner2: Long,
                 loser1: Long,
                 loser2: Long,
                 shutout: Boolean,
                 seasonId: Long,
                 created: LocalDateTime)

object Round {
  implicit val roundDecoder: Decoder[Round] = deriveDecoder[Round]

  implicit def roundEntityDecoder[F[_] : Sync]: EntityDecoder[F, Round] = jsonOf

  implicit val roundEncoder: Encoder[Round] = deriveEncoder[Round]

  implicit def roundEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Round] = jsonEncoderOf
}