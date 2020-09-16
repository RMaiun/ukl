package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class IdDto(id: Long)

object IdDto {
  implicit val idDecoder: Decoder[IdDto] = deriveDecoder[IdDto]

  implicit def idEntityDecoder[F[_] : Sync]: EntityDecoder[F, IdDto] =
    jsonOf

  implicit val idEncoder: Encoder[IdDto] = deriveEncoder[IdDto]

  implicit def idEntityEncoder[F[_] : Applicative]: EntityEncoder[F, IdDto] =
    jsonEncoderOf
}