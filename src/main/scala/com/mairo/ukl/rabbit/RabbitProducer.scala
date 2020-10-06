package com.mairo.ukl.rabbit

import cats.effect.Sync
import com.mairo.ukl.dtos.Error._
import com.mairo.ukl.dtos.{BotResponse, Error}
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.utils.ResultOps.Result
import com.rabbitmq.client.ConnectionFactory

import scala.util.Try

trait RabbitProducer[F[_]] {
  def publish(value: Result[String], key: String): F[Result[Unit]]

  def publish(data: BotResponse, key: String): F[Result[Unit]]
}

object RabbitProducer {
  def apply[F[_]](implicit ev: RabbitProducer[F]): RabbitProducer[F] = ev

  def impl[F[_] : Sync](factory: ConnectionFactory)(implicit config: Config): RabbitProducer[F] = {
    val connection = factory.newConnection()
    val channel = connection.createChannel()
    new RabbitProducer[F] {
      override def publish(value: Result[String], queue: String): F[Result[Unit]] = {
        value match {
          case Left(throwable) =>
            val error = errorEncoder.apply(Error(throwable.getMessage)).toString()
            publishInternal(error, config.rabbit.errorChannel)
          case Right(v) =>
            publishInternal(v, queue)
        }
      }

      override def publish(data: BotResponse, key: String): F[Result[Unit]] = {
        import BotResponse._
        import io.circe.syntax._
        val json = data.asJson.toString()
        publishInternal(json, key)
      }

      private def publishInternal(value: String, queue: String): F[Result[Unit]] = {
        Sync[F].delay(
          Try(channel.basicPublish("", queue, false, false, null, value.getBytes)).toEither
        )
      }
    }
  }
}
