package com.mairo.ukl.rabbit

import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}

object RabbitConfigurer {
  val EXCHANGE_NAME = "ukl_exchange"
  val INPUT_QUEUE = "input"
  val LIST_PLAYERS_RK = "list_players"
  val ADD_PLAYER_RK = "add_player"

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
