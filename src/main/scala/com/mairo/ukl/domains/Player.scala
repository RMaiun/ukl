package com.mairo.ukl.domains

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class Player(id: Long,
                  surname: String,
                  tid: Option[String],
                  cid: Option[String],
                  admin: Boolean)

object Player {

  implicit val playerDecoder: Decoder[Player] = deriveDecoder[Player]

  implicit def playerEntityDecoder[F[_] : Sync]: EntityDecoder[F, Player] =
    jsonOf

  implicit val playerEncoder: Encoder[Player] = deriveEncoder[Player]

  implicit def playerEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Player] =
    jsonEncoderOf
}
