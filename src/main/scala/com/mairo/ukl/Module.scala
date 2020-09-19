package com.mairo.ukl

import cats.Monad
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.syntax.semigroupk._
import com.mairo.ukl.rabbit.{RabbitConfigurer, RabbitConsumer, RabbitTester}
import com.mairo.ukl.repositories.PlayerRepository
import com.mairo.ukl.services.{PlayerService, UserRightsService}
import com.mairo.ukl.utils.{ConfigProvider, TransactorProvider}
import io.chrisdavenport.log4cats.Logger
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.implicits._

object Module {

  def initHttpApp[F[_] : ConcurrentEffect : Monad : Logger](client: Client[F])(implicit T: Timer[F],
                                                                               C: ContextShift[F]): HttpApp[F] = {
    // general
    val config = ConfigProvider.provideConfig
    val transactor = TransactorProvider.hikariTransactor(config, allowPublicKeyRetrieval = true)

    // repositories
    val playerRepo = PlayerRepository.impl[F](transactor)

    // services
    val userRightsService = UserRightsService.impl[F](playerRepo)
    val playerService = PlayerService.impl[F](playerRepo, userRightsService)
    val helloWorldAlg = HelloWorld.impl[F]
    val jokeAlg = Jokes.impl[F](config, client, playerRepo)

    //rabbitMQ consumers
    val connection = RabbitConfigurer.initRabbit()
    RabbitConsumer.startConsumer(connection)
    RabbitTester.startRepeatablePlayersCheck(playerService)

    // http
    val httpApp = (UklRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
      UklRoutes.jokeRoutes[F](jokeAlg)).orNotFound

    httpApp
  }
}
