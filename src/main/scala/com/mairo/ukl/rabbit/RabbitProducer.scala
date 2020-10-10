package com.mairo.ukl.rabbit

import cats.effect.Sync
import com.mairo.ukl.dtos.BotResponse
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow
import com.mairo.ukl.utils.ResultOps.Result
import com.rabbitmq.client.ConnectionFactory

import scala.util.Try

trait RabbitProducer[F[_]] {
  def publish(data: BotResponse, key: String): Flow[F, Unit]

  def publishString(data: String, key: String): Flow[F, Unit]
}

object RabbitProducer {
  def apply[F[_]](implicit ev: RabbitProducer[F]): RabbitProducer[F] = ev

  def impl[F[_] : Sync](factory: ConnectionFactory)(implicit config: Config): RabbitProducer[F] = {
    val connection = factory.newConnection()
    val channel = connection.createChannel()
    new RabbitProducer[F] {

      override def publish(data: BotResponse, key: String): Flow[F, Unit] = {
        import BotResponse._
        import io.circe.syntax._
        val json = data.asJson.toString()
        Flow(publishInternal(json, key))
      }

      override def publishString(data: String, key: String): Flow[F, Unit] = {
        Flow(publishInternal(data, key))
      }

      private def publishInternal(value: String, queue: String): F[Result[Unit]] = {
        Sync[F].delay(
          Try(channel.basicPublish("", queue, false, false, null, value.getBytes)).toEither
        )
      }
    }
  }
}
