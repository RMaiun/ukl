package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class SeasonStatsRowsDto(headers: List[String],
                              totals: List[Int],
                              games: List[List[String]],
                              createdDates: List[String],
                              roundsPlayed: Int)

object SeasonStatsRowsDto {
  implicit val seasonStatsRowsDtoDecoder: Decoder[SeasonStatsRowsDto] = deriveDecoder[SeasonStatsRowsDto]

  implicit def seasonStatsRowsDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, SeasonStatsRowsDto] = jsonOf

  implicit val seasonStatsRowsDtoEncoder: Encoder[SeasonStatsRowsDto] = deriveEncoder[SeasonStatsRowsDto]

  implicit def seasonStatsRowsDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, SeasonStatsRowsDto] = jsonEncoderOf
}
