package com.mairo.ukl.rabbit

import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

import cats.Monad
import cats.effect.{ConcurrentEffect, Sync, Timer}
import cats.syntax.flatMap._
import com.mairo.ukl.services.PlayerService
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.FlowLog
import io.chrisdavenport.log4cats.Logger

import scala.concurrent.duration.FiniteDuration

object RabbitTester {

  def startRepeatablePlayersCheck[F[_] : Monad : Timer : Sync : Logger : ConcurrentEffect](PS: PlayerService[F]): ExecutorService = {
    val pool: ExecutorService = Executors.newSingleThreadExecutor()
    pool.execute(() => {
      ConcurrentEffect[F].toIO(onStartDelay >> checkPlayers(1, PS)).unsafeRunSync()
    })
    pool
  }

  def schedule[F[_] : Monad : Timer : Logger]: F[Unit] = {
    Logger[F].info("Schedule for 3 second") >> Timer[F].sleep(FiniteDuration(50, TimeUnit.MILLISECONDS))
  }

  def onStartDelay[F[_] : Timer : Logger : Monad]: F[Unit] = {
    Logger[F].info("On Start Delay") >> Timer[F].sleep(FiniteDuration(5, TimeUnit.SECONDS))
  }

  def checkPlayers[F[_] : Monad : Timer : Logger : Sync](num: Int, PS: PlayerService[F]): F[Unit] = {
    val action = (for {
      _ <- FlowLog.info("Search for players")
      dtoOut <- PS.findAllPlayers
      key = if (num % 2 == 0) RabbitConfigurer.LIST_PLAYERS_RK else RabbitConfigurer.ADD_PLAYER_RK
      _ <- FlowLog.info(s"Going to produce msg for key= $key")
      _ <- RabbitProducer.publish(dtoOut.toString, key)
      _ <- FlowLog.info(s"Players where sent to RabbitMQ")
    } yield ()
      ).value

    action >> schedule >> checkPlayers(num + 1, PS)
  }

}
