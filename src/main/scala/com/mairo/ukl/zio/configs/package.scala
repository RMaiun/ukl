package com.mairo.ukl.zio

import pureconfig._
import pureconfig.generic.semiauto._
import zio._

package object configs {
  type HasAllConfigs = Has[AllConfigs]

  final case class AllConfigs(app: AppConfig, rabbit: RabbitConfig, server: ServerConfig)
  object AllConfigs {
    implicit val convert: ConfigConvert[AllConfigs] = deriveConvert
  }

  final case class AppConfig(
    topPlayersLimit: Int,
    winPoints: Int,
    winShutoutPoints: Int,
    losePoints: Int,
    loseShutoutPoints: Int,
    archiveReceiver: String,
    notificationsEnabled: Boolean,
    expectedGames: Int,
    reportTimezone: String,
    privileged: String,
    mongoUrl: String
  )
  object AppConfig {
    implicit val convert: ConfigConvert[AppConfig] = deriveConvert
  }

  final case class ServerConfig(port: Int)
  object ServerConfig {
    implicit val convert: ConfigConvert[ServerConfig] = deriveConvert
  }

  final case class RabbitConfig(
    username: String,
    password: String,
    host: String,
    virtualHost: String,
    port: String,
    inputQueue: String,
    outputQueue: String
  )
  object RabbitConfig {
    implicit val convert: ConfigConvert[RabbitConfig] = deriveConvert
  }

  val live: ZLayer[Any, IllegalStateException, HasAllConfigs] =
    ZIO
      .fromEither(ConfigSource.default.load[AllConfigs])
      .mapError(failures =>
        new IllegalStateException(
          s"Error loading configuration: $failures"
        )
      )
      .toLayer
}
