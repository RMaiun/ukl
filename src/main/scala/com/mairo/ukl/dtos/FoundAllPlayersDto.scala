package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import com.mairo.ukl.domains.Player
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class FoundAllPlayersDto(players: List[Player])

object FoundAllPlayersDto {

  implicit val foundAllPlayersDtoDecoder: Decoder[FoundAllPlayersDto] = deriveDecoder[FoundAllPlayersDto]

  implicit def foundAllPlayersDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, FoundAllPlayersDto] =
    jsonOf

  implicit val foundAllPlayersDtoEncoder: Encoder[FoundAllPlayersDto] = deriveEncoder[FoundAllPlayersDto]

  implicit def foundAllPlayersDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, FoundAllPlayersDto] =
    jsonEncoderOf
}
