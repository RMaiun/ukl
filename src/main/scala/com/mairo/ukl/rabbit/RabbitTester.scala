package com.mairo.ukl.rabbit

import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

import cats.Monad
import cats.effect.{ConcurrentEffect, Sync, Timer}
import cats.syntax.flatMap._
import com.mairo.ukl.dtos.BotResponse
import com.mairo.ukl.dtos.FoundAllPlayersDto.foundAllPlayersDtoEncoder
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.services.PlayerService
import com.mairo.ukl.utils.FlowLog
import io.chrisdavenport.log4cats.Logger

import scala.concurrent.duration.FiniteDuration

object RabbitTester {

  def startRepeatablePlayersCheck[F[_] : Monad : Timer : Sync : Logger : ConcurrentEffect](PS: PlayerService[F],
                                                                                           RP: RabbitProducer[F])
                                                                                          (implicit config: Config): ExecutorService = {
    val pool: ExecutorService = Executors.newSingleThreadExecutor()
    pool.execute(() => {
      ConcurrentEffect[F].toIO(onStartDelay >> checkPlayers(1, PS, RP)).unsafeRunSync()
    })
    pool
  }

  def schedule[F[_] : Monad : Timer : Logger]: F[Unit] = {
    Logger[F].info("Schedule for 3 second") >> Timer[F].sleep(FiniteDuration(1000, TimeUnit.MILLISECONDS))
  }

  def onStartDelay[F[_] : Timer : Logger : Monad]: F[Unit] = {
    Logger[F].info("On Start Delay") >> Timer[F].sleep(FiniteDuration(5, TimeUnit.SECONDS))
  }

  def checkPlayers[F[_] : Monad : Timer : Logger : Sync](num: Int, PS: PlayerService[F], RP: RabbitProducer[F])
                                                        (implicit config: Config): F[Unit] = {
    val result = for {
      _ <- FlowLog.info("Search for players")
      dtoOut <- PS.findAllPlayers
    } yield foundAllPlayersDtoEncoder.apply(dtoOut).toString()
    val mappedRes = Monad[F].map(result.value) {
      case Right(x) => x
      case Left(err) => err.getMessage
    }
    val send = Monad[F].flatMap(mappedRes)(x => RP.publish(BotResponse("test", x), config.rabbit.outputChannel).value)

    send >> schedule >> checkPlayers(num + 1, PS, RP)
  }
}
