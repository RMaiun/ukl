package com.mairo.ukl.domains

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class Season(id: Long, name: String)

object Season {

  implicit val playerDecoder: Decoder[Season] = deriveDecoder[Season]

  implicit def playerEntityDecoder[F[_] : Sync]: EntityDecoder[F, Season] =
    jsonOf

  implicit val playerEncoder: Encoder[Season] = deriveEncoder[Season]

  implicit def playerEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Season] =
    jsonEncoderOf
}
