package com.mairo.ukl.rabbit

import cats.effect.Sync
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.processor.CommandObjects.BotOutputMessage
import com.mairo.ukl.utils.flow.Flow.Flow
import com.mairo.ukl.utils.flow.ResultOps.Result
import com.mairo.ukl.utils.flow
import com.mairo.ukl.utils.flow.Flow
import com.rabbitmq.client.ConnectionFactory

import scala.util.Try

trait RabbitSender[F[_]] {
  def publish(data: BotOutputMessage): Flow[F, Unit]

  def publishString(data: String, key: String): Flow[F, Unit]
}

object RabbitSender {
  def apply[F[_]](implicit ev: RabbitSender[F]): RabbitSender[F] = ev

  def impl[F[_] : Sync](factory: ConnectionFactory)(implicit config: Config): RabbitSender[F] = {
    val connection = factory.newConnection()
    val channel = connection.createChannel()
    new RabbitSender[F] {

      override def publish(data: BotOutputMessage): Flow[F, Unit] = {
        import com.mairo.ukl.processor.CommandObjectCodecs._
        import io.circe.syntax._
        val json = data.asJson.toString()
        flow.Flow(publishInternal(json, config.rabbit.outputChannel))
      }

      override def publishString(data: String, key: String): Flow[F, Unit] = {
        flow.Flow(publishInternal(data, key))
      }

      private def publishInternal(value: String, queue: String): F[Result[Unit]] = {
        Sync[F].delay(
          Try(channel.basicPublish("", queue, false, false, null, value.getBytes)).toEither
        )
      }
    }
  }
}
