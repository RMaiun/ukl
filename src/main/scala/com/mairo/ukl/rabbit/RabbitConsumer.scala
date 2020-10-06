package com.mairo.ukl.rabbit

import cats.Monad
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.services.TelegramMsgProcessor
import com.rabbitmq.client._

object RabbitConsumer {

  def startConsumer[F[_] : Monad](connectionFactory: ConnectionFactory,
                                  MessageProcessor: TelegramMsgProcessor[F])
                                 (implicit config: Config): Unit = {
    val channel = connectionFactory.newConnection().createChannel()
    channel.basicConsume(config.rabbit.inputChannel, true, consumer(channel, "[LIST PLAYERS]", MessageProcessor))

    val channel2 = connectionFactory.newConnection().createChannel()
    channel2.basicConsume(config.rabbit.outputChannel, true, consumer2(channel2, "[ERROR]"))

    val channel3 = connectionFactory.newConnection().createChannel()
    channel3.basicConsume(config.rabbit.errorChannel, true, consumer3(channel3, "[ERROR]"))

  }

  def consumer[F[_] : Monad](channel: Channel, logPrefix: String, MessageProcessor: TelegramMsgProcessor[F]): Consumer = {
    new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        println("=-=-=-=-=-Received INPUT=-=-=-=-=-=-=-")
        MessageProcessor.processMsg(new String(body))
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
