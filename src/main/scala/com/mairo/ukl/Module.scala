package com.mairo.ukl

import cats.Monad
import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import cats.syntax.semigroupk._
import com.mairo.ukl.bot.BotCmdProcessor
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.helper.{ConfigProvider, TransactorProvider}
import com.mairo.ukl.rabbit.{RabbitConfigurer, RabbitConsumer, RabbitSender}
import com.mairo.ukl.repositories.{PlayerRepository, RoundRepository, SeasonRepository}
import com.mairo.ukl.services._
import com.rabbitmq.client.ConnectionFactory
import io.chrisdavenport.log4cats.Logger
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.implicits._

object Module {

  def initHttpApp[F[_] : ConcurrentEffect : Monad : Logger](client: Client[F])(implicit T: Timer[F],
                                                                               C: ContextShift[F]): HttpApp[F] = {
    // general
    implicit val config: ConfigProvider.Config = ConfigProvider.provideConfig
    val transactor = TransactorProvider.hikariTransactor(config, allowPublicKeyRetrieval = true)

    // repositories
    val playerRepo = PlayerRepository.impl[F](transactor)
    val seasonRepo = SeasonRepository.impl[F](transactor)
    val roundRepo = RoundRepository.impl[F](transactor)

    // services
    val seasonService = SeasonService.impl[F](seasonRepo)
    val userRightsService = UserRightsService.impl[F](playerRepo)
    val playerService = PlayerService.impl[F](playerRepo, userRightsService)
    val roundService = RoundService.impl[F](playerService, seasonService, roundRepo, userRightsService)
    val statsService = StatisticService.impl[F](roundService, config.app)
    val helloWorldAlg = HelloWorld.impl[F]
    //rabbitMQ consumers
    val factory = RabbitConfigurer.factory(config)
    val connection = RabbitConfigurer.initRabbit(factory)
    val rabbitProducer = RabbitSender.impl[F](factory)

    val botCmdProcessor = BotCmdProcessor.impl(playerService,roundService, rabbitProducer)
    val messageProcessor = TelegramMsgProcessor.impl[F](botCmdProcessor)

    RabbitConsumer.startConsumer(factory, messageProcessor)
    val jokeAlg = Jokes.impl[F](config, client, playerRepo, rabbitProducer)

    // for testing
    //    RabbitTester.startRepeatablePlayersCheck(playerService,rabbitProducer)

    // http
    val httpApp = (UklRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
      UklRoutes.jokeRoutes[F](jokeAlg)).orNotFound

    httpApp
  }

  def startConsumer[F[_] : Monad : Sync : ContextShift : ConcurrentEffect](connectionFactory: ConnectionFactory,
                                                                           MessageProcessor: TelegramMsgProcessor[F])
                                                                          (implicit config: Config): Unit = {

  }
}
