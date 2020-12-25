package com.mairo.ukl

import java.util.concurrent.Executors

import cats.Monad
import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import fs2.Stream
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import cats.implicits._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

object UklServer {
  implicit def unsafeLogger[F[_] : Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  val clientEC: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

  def stream[F[_] : ConcurrentEffect](implicit T: Timer[F],
                                      C: ContextShift[F],
                                      M: Monad[F]): Stream[F, Nothing] = {
    for {
      //general
      client <- BlazeClientBuilder[F](global).withMaxWaitQueueLimit(1000).stream
      httpApp <- Stream.eval(Module.initHttpApp(client).pure[F])

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)
      exitCode <- BlazeServerBuilder[F](clientEC)
        .bindHttp(8081, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
