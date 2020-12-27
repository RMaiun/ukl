package com.mairo.ukl.rabbit

import java.util.concurrent.Executors

import cats.effect.{ConcurrentEffect, ContextShift, Sync}
import cats.{Monad, MonadError}
import com.mairo.ukl.helper.ConfigProvider.Config
import com.rabbitmq.client._

import scala.concurrent.ExecutionContext

object RabbitConsumer {
  private val ec = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

  def startConsumer[F[_] : Monad : Sync : ContextShift : ConcurrentEffect](connectionFactory: ConnectionFactory,
                                                                           MessageProcessor: TelegramBotCommandHandler[F])
                                                                          (implicit config: Config): Unit = {
    val channel = connectionFactory.newConnection().createChannel()
    channel.basicConsume(config.rabbit.inputChannel, true, consumer(channel,MessageProcessor))
  }

  def consumer[F[_] : Sync : ConcurrentEffect : ContextShift](channel: Channel,
                                                              cmdHandler: TelegramBotCommandHandler[F])
                                                             (implicit MT: MonadError[F, Throwable]): Consumer = {
    new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        println("=-=-=-=-=-Received INPUT=-=-=-=-=-=-=-")
        val effect = cmdHandler.handleCmd(new String(body)).value
        val handledEffect = MT.flatMap(effect) {
          case Right(v) => MT.pure(())
          case Left(err) => Sync[F].delay(err.printStackTrace())
        }
        ConcurrentEffect[F].toIO(handledEffect).unsafeRunAsyncAndForget()
      }
    }
  }
}
