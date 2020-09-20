package com.mairo.ukl.rabbit

import java.util.concurrent.Executors

import com.mairo.ukl.utils.ConfigProvider.Config
import com.rabbitmq.client.{Connection, ConnectionFactory}

object RabbitConfigurer {

  def factory(config: Config): ConnectionFactory = {
    val factory: ConnectionFactory = new ConnectionFactory
    factory.setUsername(config.rabbit.global.username)
    factory.setPassword(config.rabbit.global.password)
    factory.setVirtualHost(config.rabbit.global.virtualHost)
    factory.setHost(config.rabbit.global.host)
    factory.setPort(config.rabbit.global.port)
    factory
  }


  def initRabbit(factory: ConnectionFactory)(implicit config: Config): Connection = {
    val connection: Connection = factory.newConnection(Executors.newCachedThreadPool())
    initStructure(connection)
    connection
  }

  private def initStructure(connection: Connection)(implicit config: Config): Unit = {
    val channel = connection.createChannel()
    channel.exchangeDeclare(config.rabbit.exName, config.rabbit.exType)
    channel.queueDeclare(config.rabbit.listPlayersQueue.name, false, false, false, null)
    channel.queueDeclare(config.rabbit.addPlayerQueue.name, false, false, false, null)
    channel.queueDeclare(config.rabbit.errorsQueue.name, false, false, false, null)
    channel.queueBind(config.rabbit.listPlayersQueue.name, config.rabbit.exName, config.rabbit.listPlayersQueue.key)
    channel.queueBind(config.rabbit.addPlayerQueue.name, config.rabbit.exName, config.rabbit.addPlayerQueue.key)
    channel.queueBind(config.rabbit.errorsQueue.name, config.rabbit.exName, config.rabbit.errorsQueue.key)
    channel.close()
  }
}
