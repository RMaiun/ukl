package com.mairo.ukl.rabbit

import java.util.concurrent.Executors

import com.mairo.ukl.rabbit.RabbitConfigurer.EXCHANGE_NAME
import com.rabbitmq.client._

object RabbitConsumer {

  def startConsumer(): Channel = {
    val connection: Connection = RabbitConfigurer.factory.newConnection(Executors.newCachedThreadPool())
    val channel = connection.createChannel()
    channel.exchangeDeclare(EXCHANGE_NAME, "direct")
    channel.queueDeclare("listPlayersQueue", false, false, false, null)
    channel.queueDeclare("addPlayerQueue", false, false, false, null)
    channel.queueBind("listPlayersQueue", EXCHANGE_NAME, RabbitConfigurer.LIST_PLAYERS_RK)
    channel.queueBind("addPlayerQueue", EXCHANGE_NAME, RabbitConfigurer.ADD_PLAYER_RK)

    val consumer1 = new DefaultConsumer(channel){
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        val message = new String(body, "UTF-8")
        println(s"${Thread.currentThread().getName} [LIST PLAYERS] Received '" + message + "'")
      }
    }

    val channel2 = connection.createChannel()

    val consumer2 = new DefaultConsumer(channel2){
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        val message = new String(body, "UTF-8")
        println(s"${Thread.currentThread().getName} [ADD PLAYER] Received '" + message + "'")
      }
    }
    channel.basicConsume("listPlayersQueue", true, consumer1)

    channel2.basicConsume("addPlayerQueue", true, consumer2)

    channel
  }
}
