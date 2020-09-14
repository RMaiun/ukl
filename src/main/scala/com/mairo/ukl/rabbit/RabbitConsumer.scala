package com.mairo.ukl.rabbit

import java.util.concurrent.Executors

import com.rabbitmq.client._

object RabbitConsumer {

  def startConsumer(): Channel = {
    val connection: Connection = RabbitConfigurer.factory.newConnection(Executors.newCachedThreadPool())
    val channel = connection.createChannel()

    val callback: DeliverCallback = (consumerTag: String, delivery: Delivery) => {
      val message = new String(delivery.getBody, "UTF-8")
      System.out.println(" [x] Received '" + message + "'")
    }

    val shutdownSignalCallback = new ConsumerShutdownSignalCallback {
      override def handleShutdownSignal(consumerTag: String, sig: ShutdownSignalException): Unit = {
        sig.printStackTrace()
      }
    }
    channel.basicConsume(RabbitConfigurer.EXCHANGE, true, callback, shutdownSignalCallback)
    channel
  }
}
