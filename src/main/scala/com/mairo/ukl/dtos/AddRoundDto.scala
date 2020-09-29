package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class AddRoundDto(w1: String,
                       w2: String,
                       l1: String,
                       l2: String,
                       shutout: Boolean,
                       moderator: String)

object AddRoundDto{
  implicit val addRoundDtoDecoder: Decoder[AddRoundDto] = deriveDecoder[AddRoundDto]

  implicit def addRoundDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, AddRoundDto] = jsonOf

  implicit val addRoundDtoEncoder: Encoder[AddRoundDto] = deriveEncoder[AddRoundDto]

  implicit def addRoundDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, AddRoundDto] = jsonEncoderOf
}
