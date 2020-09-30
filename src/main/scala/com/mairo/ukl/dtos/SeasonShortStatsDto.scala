package com.mairo.ukl.dtos

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class SeasonShortStatsDto(season: String,
                               playersRating: List[PlayerStatsDto],
                               gamesPlayed: Int,
                               daysToSeasonEnd: Int,
                               bestStreak: StreakDto,
                               worstStreak: StreakDto)

object SeasonShortStatsDto {
  implicit val seasonShortStatsDtoDecoder: Decoder[SeasonShortStatsDto] = deriveDecoder[SeasonShortStatsDto]

  implicit def seasonShortStatsDtoEntityDecoder[F[_] : Sync]: EntityDecoder[F, SeasonShortStatsDto] = jsonOf

  implicit val seasonShortStatsDtoEncoder: Encoder[SeasonShortStatsDto] = deriveEncoder[SeasonShortStatsDto]

  implicit def seasonShortStatsDtoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, SeasonShortStatsDto] = jsonEncoderOf

}