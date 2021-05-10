package com.mairo.ukl.zio

import cats.data.Kleisli
import cats.effect._
import com.mairo.ukl.zio.configs.layers
import com.mairo.ukl.zio.services.SeasonService
import fs2.Stream.Compiler._
import org.http4s.{Header, HttpApp, HttpRoutes, Request, Status}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio.clock.Clock
import zio.interop.catz._
import zio.{ExitCode => ZExitCode, _}
object Main extends zio.App {
  type AppTask[A] = RIO[layers.AppEnv with Clock, A]

  override def run(args: List[String]): URIO[zio.ZEnv, ZExitCode] = {
    val prog =
      for {
        _      <- logging.log.info(s"Starting...")
        httpApp = Router[AppTask](
          "/season" -> myMiddle(SeasonService.routes(), Header("SomeKey", "SomeValue"))
        ).orNotFound

        _ <- runHttp(httpApp, 9001)
      } yield ZExitCode.success

    prog
      .provideSomeLayer[ZEnv](layers.live.appLayer)
      .orDie
  }

  def myMiddle(service: HttpRoutes[AppTask], header: Header): HttpRoutes[AppTask] = Kleisli { req: Request[AppTask] =>
    service(req).map {
      case Status.Successful(resp) =>
        resp.putHeaders(header)
      case resp =>
        resp
    }
  }

  def runHttp[R <: Clock](
                           httpApp: HttpApp[RIO[R, *]],
                           port: Int
                         ): ZIO[R, Throwable, Unit] = {
    type Task[A] = RIO[R, A]
    ZIO.runtime[R].flatMap { implicit rts =>
      BlazeServerBuilder
        .apply[Task](rts.platform.executor.asEC)
        .bindHttp(port, "localhost")
        .withHttpApp(httpApp)
        .serve
        .compile[Task, Task, ExitCode]
        .drain
    }
  }
}