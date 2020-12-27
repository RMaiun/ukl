package com.mairo.ukl.helper

import pureconfig.ConfigSource
import pureconfig._
import pureconfig.generic.auto._

object ConfigProvider {

  case class AppConfig(topPlayersLimit: Int,
                       minGames: Int,
                       winPoints: Int,
                       winShutoutPoints: Int,
                       losePoints: Int,
                       loseShutoutPoints: Int,
                       archiveReceiver: String,
                       notificationsEnabled: Boolean)

  case class DbConfig(host: String,
                      port: Int,
                      database: String,
                      username: String,
                      password: String)

  case class ServerConfig(port: Int)

  case class Config(app: AppConfig,
                    db: DbConfig,
                    rabbit: RabbitConfig,
                    server: ServerConfig)

  case class RabbitConfig(global: RabbitGlobalConfig,
                          inputChannel: String,
                          outputChannel: String,
                          binaryChannel: String,
                          errorChannel: String
                         )

  case class RabbitGlobalConfig(username: String,
                                password: String,
                                host: String,
                                virtualHost: String,
                                port: Int)


  def provideConfig: Config = {
    ConfigSource.default.loadOrThrow[Config]
  }
}
