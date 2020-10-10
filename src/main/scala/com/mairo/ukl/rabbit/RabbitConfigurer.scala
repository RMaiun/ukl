package com.mairo.ukl.rabbit

import java.util.concurrent.Executors

import com.mairo.ukl.helper.ConfigProvider.Config
import com.rabbitmq.client.{Connection, ConnectionFactory}

object RabbitConfigurer {

  def factory(config: Config): ConnectionFactory = {
    val factory: ConnectionFactory = new ConnectionFactory
    factory.setAutomaticRecoveryEnabled(true)
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
    channel.queueDeclare(config.rabbit.inputChannel, false, false, false, null)
    channel.queueDeclare(config.rabbit.outputChannel, false, false, false, null)
    channel.queueDeclare(config.rabbit.errorChannel, false, false, false, null)
    channel.queueDeclare(config.rabbit.binaryChannel, false, false, false, null)
    channel.close()
  }
}
