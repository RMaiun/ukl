package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class BotResponse(chatId: String, result: String)

object BotResponse {
  implicit val addRoundDtoDecoder: Decoder[BotResponse] = deriveDecoder[BotResponse]

  implicit def addRoundDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, BotResponse] = jsonOf

  implicit val addRoundDtoEncoder: Encoder[BotResponse] = deriveEncoder[BotResponse]

  implicit def addRoundDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, BotResponse] = jsonEncoderOf
}