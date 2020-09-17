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
      ConcurrentEffect[F].toIO(onStartDelay >> checkPlayers(PS)).unsafeRunSync()
    })
    pool
  }

  def schedule[F[_] : Monad : Timer : Logger]: F[Unit] = {
    Logger[F].info("Schedule for 3 second") >> Timer[F].sleep(FiniteDuration(3, TimeUnit.SECONDS))
  }

  def onStartDelay[F[_] : Timer : Logger : Monad]: F[Unit] = {
    Logger[F].info("On Start Delay") >> Timer[F].sleep(FiniteDuration(5, TimeUnit.SECONDS))
  }

  def checkPlayers[F[_] : Monad : Timer : Logger : Sync](PS: PlayerService[F]): F[Unit] = {
    val action = (for {
      _ <- Flow.log(Logger[F].info("Search for players"))
      dtoOut <- PS.findAllPlayers
      _ <- FlowLog.info(s"Going to produce ${dtoOut.players.size} players")
      _ <- RabbitProducer.publish(dtoOut.toString)
      _ <- Flow.log(Logger[F].info(s"Players where sent to RabbitMQ"))
    } yield ()
      ).value

    action >> schedule >> checkPlayers(PS)
  }

}
