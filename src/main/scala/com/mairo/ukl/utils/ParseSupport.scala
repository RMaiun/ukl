package com.mairo.ukl.utils

import cats.Monad
import com.mairo.ukl.errors.UklException.AbsentCommandInput
import com.mairo.ukl.utils.flow.Flow
import com.mairo.ukl.utils.flow.Flow.Flow
import io.circe.{Decoder, Json}

trait ParseSupport[F[_]] {
  def parse[T](data: Option[Json])(implicit F: Monad[F], decoder: Decoder[T]): Flow[F, T] = {
    for {
      json <- Flow.fromOption(data, AbsentCommandInput())
      data <- Flow.fromRes(json.as[T])
    } yield data
  }
}
