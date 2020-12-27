package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class SeasonDto(season: String)

object SeasonDto {
  implicit val SeasonDtoDecoder: Decoder[SeasonDto] = deriveDecoder[SeasonDto]

  implicit def SeasonDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, SeasonDto] = jsonOf

  implicit val SeasonDtoEncoder: Encoder[SeasonDto] = deriveEncoder[SeasonDto]

  implicit def SeasonDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, SeasonDto] = jsonEncoderOf

}
