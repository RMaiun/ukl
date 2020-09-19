package com.mairo.ukl.rabbit

import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

import cats.Monad
import cats.effect.{ConcurrentEffect, Sync, Timer}
import cats.syntax.either._
import cats.syntax.flatMap._
import com.mairo.ukl.dtos.FoundAllPlayersDto
import com.mairo.ukl.services.PlayerService
import com.mairo.ukl.utils.Flow.{FlowLog, Result}
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
    Logger[F].info("Schedule for 3 second") >> Timer[F].sleep(FiniteDuration(1000, TimeUnit.MILLISECONDS))
  }

  def onStartDelay[F[_] : Timer : Logger : Monad]: F[Unit] = {
    Logger[F].info("On Start Delay") >> Timer[F].sleep(FiniteDuration(5, TimeUnit.SECONDS))
  }

  def checkPlayers[F[_] : Monad : Timer : Logger : Sync](num: Int, PS: PlayerService[F]): F[Unit] = {
    val action = (for {
      _ <- FlowLog.info("Search for players")
      dtoOut <- PS.findAllPlayers
      _ <- FlowLog.info(s"Going to produce msg for key= ${dto(num, dtoOut)._2}")
      _ <- FlowLog.info(s"Players where sent to RabbitMQ")
      _ <- RabbitProducer.publish(dto(num, dtoOut)._1, dto(num,dtoOut)._2 )

    } yield ()
      ).value

    action >> schedule >> checkPlayers(num + 1, PS)
  }

  def dto(num: Int, players: FoundAllPlayersDto): (Result[FoundAllPlayersDto], String) = {
    if (num % 10 == 0) {
      (new RuntimeException(s"WOW such a bad error $num").asLeft[FoundAllPlayersDto], RabbitConfigurer.errorsQR.key)
    } else if (num % 3 == 0) {
      (players.asRight[Throwable], RabbitConfigurer.listPlayersQR.key)
    } else {
      (players.asRight[Throwable], RabbitConfigurer.addPlayerQR.key)
    }
  }

}
