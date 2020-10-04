package com.mairo.ukl.rabbit

import cats.effect.Sync
import com.mairo.ukl.dtos.Error
import com.mairo.ukl.dtos.Error._
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.utils.ResultOps.Result
import com.rabbitmq.client.ConnectionFactory

import scala.util.Try

trait RabbitProducer[F[_]] {
  def publish(value: Result[String], key: String): F[Result[Unit]]
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
            publish(error, config.rabbit.errorChannel)
          case Right(v) =>
            publish(v, queue)
        }
      }

      private def publish(value: String, queue: String): F[Result[Unit]] = {
        Sync[F].delay(
          Try(channel.basicPublish("", queue, false, false, null, value.getBytes)).toEither
        )
      }
    }
  }
}
