package com.mairo.ukl

import cats.Monad
import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import cats.implicits._
import com.mairo.ukl.repositories.PlayerRepository
import com.mairo.ukl.utils.{ConfigProvider, TransactorProvider}
import fs2.Stream
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object UklServer {
  implicit def unsafeLogger[F[_] : Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]


  def stream[F[_] : ConcurrentEffect](implicit T: Timer[F],
                                      C: ContextShift[F],
                                      M: Monad[F]): Stream[F, Nothing] = {
    for {
      client <- BlazeClientBuilder[F](global).stream
      config = ConfigProvider.provideConfig
      xa = TransactorProvider.hikariTransactor(config)
      playerRepo = PlayerRepository.impl(xa)

      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg = Jokes.impl[F](config, client, unsafeLogger, playerRepo)

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = (
        UklRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
          UklRoutes.jokeRoutes[F](jokeAlg)
        ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
