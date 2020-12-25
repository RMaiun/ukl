package com.mairo.ukl.processor

import cats.Applicative
import cats.effect.Sync
import com.mairo.ukl.dtos.AddPlayerDto
import com.mairo.ukl.processor.CommandObjects.{BotInputMessage, BotOutputMessage}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

object CommandObjectCodecs {
  implicit val BotInputMessageDecoder: Decoder[BotInputMessage] = deriveDecoder[BotInputMessage]

  implicit def BotInputMessageEntityDecoder[F[_] : Sync]: EntityDecoder[F, AddPlayerDto] = jsonOf

  implicit val BotOutputMessageDecoder: Decoder[BotOutputMessage] = deriveDecoder[BotOutputMessage]

  implicit def BotOutputMessageEntityDecoder[F[_] : Sync]: EntityDecoder[F, BotOutputMessage] = jsonOf

  implicit val BotInputMessageEncoder: Encoder[BotInputMessage] = deriveEncoder[BotInputMessage]

  implicit def BotInputMessageEntityEncoder[F[_] : Applicative]: EntityEncoder[F, BotInputMessage] = jsonEncoderOf

  implicit val BotOutputMessageEncoder: Encoder[BotOutputMessage] = deriveEncoder[BotOutputMessage]

  implicit def BotOutputMessageEntityEncoder[F[_] : Applicative]: EntityEncoder[F, BotOutputMessage] = jsonEncoderOf
}
