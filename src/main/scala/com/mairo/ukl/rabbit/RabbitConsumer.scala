package com.mairo.ukl.rabbit

import java.util.concurrent.Executors

import cats.effect.{ConcurrentEffect, ContextShift, Sync}
import cats.{Monad, MonadError}
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.services.TelegramMsgProcessor
import com.mairo.ukl.utils.Flow
import com.rabbitmq.client._

import scala.concurrent.ExecutionContext

object RabbitConsumer {
  private val ec = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

  def startConsumer[F[_] : Monad : Sync : ContextShift: ConcurrentEffect](connectionFactory: ConnectionFactory,
                                                        MessageProcessor: TelegramMsgProcessor[F])
                                                       (implicit config: Config): Unit = {
    val channel = connectionFactory.newConnection().createChannel()
    channel.basicConsume(config.rabbit.inputChannel, true, consumer(channel, "[LIST PLAYERS]", MessageProcessor))

    val channel2 = connectionFactory.newConnection().createChannel()
    channel2.basicConsume(config.rabbit.outputChannel, true, consumer2(channel2, "[ERROR]"))

    val channel3 = connectionFactory.newConnection().createChannel()
    channel3.basicConsume(config.rabbit.errorChannel, true, consumer3(channel3, "[ERROR]"))

  }

  def consumer[F[_] : Sync : ConcurrentEffect : ContextShift](channel: Channel,
                                                                           logPrefix: String,
                                                                           MessageProcessor: TelegramMsgProcessor[F])
                                                             (implicit MT: MonadError[F,Throwable]): Consumer = {
    new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        println("=-=-=-=-=-Received INPUT=-=-=-=-=-=-=-")
        val effect = MessageProcessor.processMsg(new String(body)).value
        val handledEffect = MT.flatMap(effect){
          case Right(v) => MT.pure(())
          case Left(err) => Sync[F].delay(err.printStackTrace())
        }
        val shiftEffect = ContextShift[F].evalOn(ec)(handledEffect)
        ConcurrentEffect[F].toIO(shiftEffect).unsafeRunAsyncAndForget()
      }
    }
  }

  def consumer2(channel: Channel, logPrefix: String): Consumer = {
    new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        println("=-=-=-=-=-Received OUTPUT=-=-=-=-=-=-=-")
        println(new String(body))
      }
    }
  }

  def consumer3(channel: Channel, logPrefix: String): Consumer = {
    new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        println("=-=-=-=-=-Received ERROR=-=-=-=-=-=-=-")
        println(new String(body))
      }
    }
  }
}
