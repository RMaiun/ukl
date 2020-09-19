package com.mairo.ukl.rabbit

import java.util.concurrent.Executors

import com.rabbitmq.client.{Connection, ConnectionFactory}

object RabbitConfigurer {

  case class QueueRouting(name: String, key: String)

  val EXCHANGE_NAME = "ukl_exchange"
  val INPUT_QUEUE = "input"
  val listPlayersQR = QueueRouting("listPlayersQueue", "list_players")
  val addPlayerQR = QueueRouting("addPlayerQueue", "add_player")
  val errorsQR = QueueRouting("errorsQueue", "errors")

  val factory = new ConnectionFactory
  factory.setUsername("rabbitmq")
  factory.setPassword("rabbitmq")
  factory.setVirtualHost("ukl")
  factory.setHost("localhost")
  factory.setPort(5672)

  def initRabbit(): Connection = {
    val connection: Connection = factory.newConnection(Executors.newCachedThreadPool())
    initStructure(connection)
    connection
  }

  private def initStructure(connection: Connection): Unit = {
    val channel = connection.createChannel()
    channel.exchangeDeclare(EXCHANGE_NAME, "direct")
    channel.queueDeclare(listPlayersQR.name, false, false, false, null)
    channel.queueDeclare(addPlayerQR.name, false, false, false, null)
    channel.queueDeclare(errorsQR.name, false, false, false, null)
    channel.queueBind(listPlayersQR.name, EXCHANGE_NAME, listPlayersQR.key)
    channel.queueBind(addPlayerQR.name, EXCHANGE_NAME, addPlayerQR.key)
    channel.queueBind(errorsQR.name, EXCHANGE_NAME, errorsQR.key)
    channel.close()
  }
}
