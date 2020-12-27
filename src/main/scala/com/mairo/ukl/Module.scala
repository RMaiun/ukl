package com.mairo.ukl

import cats.Monad
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.syntax.semigroupk._
import com.mairo.ukl.helper.{ConfigProvider, TransactorProvider}
import com.mairo.ukl.postprocessor.PostProcessor
import com.mairo.ukl.processor.CommandProcessor
import com.mairo.ukl.rabbit.{RabbitConfigurer, RabbitConsumer, RabbitSender, TelegramBotCommandHandler}
import com.mairo.ukl.repositories.{PlayerRepository, RoundRepository, SeasonRepository}
import com.mairo.ukl.services._
import com.rabbitmq.client.{Channel, Connection}
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
    val subscriptionService = SubscriptionService.impl(userRightsService, playerService)

    val processors = CommandProcessor.allProcessors(playerService, roundService, statsService, subscriptionService)
    val helloWorldAlg = HelloWorld.impl[F]
    //rabbitMQ consumers
    val factory = RabbitConfigurer.factory(config)
    val connection: Connection = factory.newConnection()
    val channel: Channel = connection.createChannel()
    val value = channel.queueDeclare(config.rabbit.inputChannel, true, false, false, null)
    val value2 = channel.queueDeclare(config.rabbit.outputChannel, true, false, false, null)
    connection.close()

    val rabbitSender = RabbitSender.impl[F](factory)


    val postProcessors = PostProcessor.allPostProcessors(playerService, rabbitSender)

    val telegramBotCommandHandler = TelegramBotCommandHandler.impl(processors, postProcessors, rabbitSender)

    RabbitConsumer.startConsumer(factory, telegramBotCommandHandler)
    val jokeAlg = Jokes.impl[F](config, client, playerRepo, rabbitSender)

    // for testing
    //    RabbitTester.startRepeatablePlayersCheck(playerService,rabbitProducer)

    // http
    val httpApp = (UklRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
      UklRoutes.jokeRoutes[F](jokeAlg)).orNotFound

    httpApp
  }
}
