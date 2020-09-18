package com.mairo.ukl.rabbit

import cats.effect.Sync
import com.mairo.ukl.rabbit.RabbitConfigurer.EXCHANGE_NAME
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow
import com.rabbitmq.client.Connection

import scala.util.Try

object RabbitProducer {
  private val connection: Connection = RabbitConfigurer.factory.newConnection()
  private val channel = connection.createChannel()

  def publish[F[_] : Sync](value: String, key:String): Flow[F, Unit] = {
    Flow(Sync[F].delay(
      Try(channel.basicPublish(EXCHANGE_NAME, key, false, false, null, value.getBytes))
        .toEither
    ))
  }

}
