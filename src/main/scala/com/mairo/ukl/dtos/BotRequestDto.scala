package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class BotRequestDto(
                          cmd: String,
                          chatId: String,
                          data: Json)

object BotRequestDto {
  implicit val botRequestDtoDecoder: Decoder[BotRequestDto] = deriveDecoder[BotRequestDto]

  implicit def botRequestDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, BotRequestDto] = jsonOf

  implicit val botRequestDtoEncoder: Encoder[BotRequestDto] = deriveEncoder[BotRequestDto]

  implicit def botRequestDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, BotRequestDto] = jsonEncoderOf

}
