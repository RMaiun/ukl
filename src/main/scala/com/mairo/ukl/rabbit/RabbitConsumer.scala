package com.mairo.ukl.rabbit

import com.mairo.ukl.rabbit.RabbitConfigurer._
import com.mairo.ukl.helper.ConfigProvider.Config
import com.rabbitmq.client._

object RabbitConsumer {

  def startConsumer(connection: Connection)(implicit config:Config): Unit = {
    val channel = connection.createChannel()

    channel.basicConsume(config.rabbit.outputChannel, true, consumer(channel, "[LIST PLAYERS]"))

    val channel3 = connection.createChannel()
    channel3.basicConsume(config.rabbit.errorChannel, true, consumer(channel3, "[BLABLABLA]"))
  }

  def consumer(channel: Channel, logPrefix: String): Consumer = {
    new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        val message = new String(body, "UTF-8")
        println(s"${Thread.currentThread().getName} $logPrefix Received '" + message + "'")
      }
    }
  }
}
