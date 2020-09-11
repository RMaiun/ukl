package com.mairo.ukl

import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Monad}
import com.mairo.ukl.utils.Flow.Result
import io.chrisdavenport.log4cats.Logger
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes, Response}

object UklRoutes {

  case class Error(msg: String)

  implicit val errorEncoder: Encoder[Error] = deriveEncoder[Error]

  implicit def errorEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Error] = jsonEncoderOf


  def response[F[_] : Sync : Monad : Logger, T](flow: F[Result[T]])(implicit ee: EntityEncoder[F, T]): F[Response[F]] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    Monad[F].flatMap(flow) {
      case Left(err) =>
        for {
          _ <- Sync[F].delay(err.printStackTrace())
          x <- Response[F](status = BadRequest).withEntity(Error(err.getMessage)).pure[F]
        } yield x
      case Right(value) => Ok(value)
    }
  }

  def jokeRoutes[F[_] : Sync : Monad : Logger](J: Jokes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "joke" =>
        response(J.get)
    }
  }

  def helloWorldRoutes[F[_] : Sync](H: HelloWorld[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        for {
          greeting <- H.hello(HelloWorld.Name(name))
          resp <- Ok(greeting)
        } yield resp
    }
  }
}