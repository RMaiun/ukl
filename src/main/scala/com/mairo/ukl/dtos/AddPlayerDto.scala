package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class AddPlayerDto(surname: String,
                        tid: Option[String],
                        admin: Boolean,
                        moderator: String)

object AddPlayerDto {
  implicit val addPlayerDtoDecoder: Decoder[AddPlayerDto] = deriveDecoder[AddPlayerDto]

  implicit def addPlayerDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, AddPlayerDto] = jsonOf

  implicit val addPlayerDtoEncoder: Encoder[AddPlayerDto] = deriveEncoder[AddPlayerDto]

  implicit def addPlayerDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, AddPlayerDto] = jsonEncoderOf
}
