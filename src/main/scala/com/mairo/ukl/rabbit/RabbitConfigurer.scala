package com.mairo.ukl.rabbit

import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}

object RabbitConfigurer {

  val EXCHANGE = "hello"

  val factory = new ConnectionFactory
  factory.setUsername("rabbitmq")
  factory.setPassword("rabbitmq")
  factory.setVirtualHost("ukl")
  factory.setHost("localhost")
  factory.setPort(5672)

  def initRabbit(): Connection = {
    val conn: Connection = factory.newConnection
    val channel: Channel = conn.createChannel()
    channel.queueDeclare("quick-start", false, false, false, null)
    channel.close()
    conn
  }
}
