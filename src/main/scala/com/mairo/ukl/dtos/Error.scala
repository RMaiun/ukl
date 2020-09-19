package com.mairo.ukl.dtos

import cats.Applicative
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

case class Error(msg: String)

object Error {
  implicit val errorEncoder: Encoder[Error] = deriveEncoder[Error]

  implicit def errorEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Error] = jsonEncoderOf
}
