package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class LinkTidDto(tid: String, nameToLink: String, moderator: String)

object LinkTidDto {
  implicit val LinkTidDtoDecoder: Decoder[LinkTidDto] = deriveDecoder[LinkTidDto]

  implicit def LinkTidDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, LinkTidDto] = jsonOf

  implicit val LinkTidDtoEncoder: Encoder[LinkTidDto] = deriveEncoder[LinkTidDto]

  implicit def LinkTidDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, LinkTidDto] = jsonEncoderOf
}
