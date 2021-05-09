package com.mairo.ukl.zio.configs

import com.mairo.ukl.zio.configs.DbClient.HasDb
import com.mairo.ukl.zio.repositories.SeasonRepository
import com.mairo.ukl.zio.repositories.SeasonRepository.HasSeasonRepo
import zio.ZLayer
import zio.blocking.Blocking
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger

object layers {
  type Layer0Env = Logging with Blocking
  type Layer1Env = Layer0Env with HasDb
  type Layer2Env = Layer1Env with HasSeasonRepo
  type AppEnv    = Layer2Env

  object live {
    val layer0: ZLayer[Blocking, Throwable, Layer0Env]  = Blocking.any ++ Slf4jLogger.make((_, msg) => msg)
    val layer1: ZLayer[Layer0Env, Throwable, Layer1Env] = DbClient.live ++ ZLayer.identity[Layer0Env]
    val layer2: ZLayer[Layer1Env, Throwable, Layer2Env] = SeasonRepository.live ++ ZLayer.identity[Layer1Env]

    val appLayer: ZLayer[Blocking, Throwable, AppEnv] = layer0 >>> layer1 >>> layer2
  }

}
