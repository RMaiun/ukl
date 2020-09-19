package com.mairo.ukl.rabbit

import cats.effect.Sync
import com.mairo.ukl.dtos.Error
import com.mairo.ukl.dtos.Error._
import com.mairo.ukl.rabbit.RabbitConfigurer.EXCHANGE_NAME
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.{Flow, Result}
import com.rabbitmq.client.Connection
import io.circe.Encoder

import scala.util.Try

object RabbitProducer {
  private val connection: Connection = RabbitConfigurer.factory.newConnection()
  private val channel = connection.createChannel()

  private def publish[F[_] : Sync](value: String, key: String): Flow[F, Unit] = {
    Flow(
      Sync[F].delay(
        Try(channel.basicPublish(EXCHANGE_NAME, key, false, false, null, value.getBytes)).toEither
      )
    )
  }

  def publish[F[_] : Sync, T](value: Result[T], key: String)(implicit encoder: Encoder[T]): Flow[F, Unit] = {
    value match {
      case Left(throwable) =>
        val error = errorEncoder.apply(Error(throwable.getMessage)).toString()
        publish(error, RabbitConfigurer.errorsQR.key)
      case Right(v) =>
        val jsonValue = encoder.apply(v).toString()
        publish(jsonValue, key)
    }
  }
}
