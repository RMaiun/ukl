package com.mairo.ukl

import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Monad}
import com.mairo.ukl.repositories.PlayerRepository
import com.mairo.ukl.services.KafkaProducer
import com.mairo.ukl.utils.ConfigProvider.Config
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Result
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits._
import org.http4s.{EntityDecoder, EntityEncoder}

trait Jokes[F[_]] {
  def get: F[Result[Jokes.Joke]]
}

object Jokes {
  def apply[F[_]](implicit ev: Jokes[F]): Jokes[F] = ev

  final case class Joke(joke: String) extends AnyVal

  object Joke {
    implicit val jokeDecoder: Decoder[Joke] = deriveDecoder[Joke]

    implicit def jokeEntityDecoder[F[_] : Sync]: EntityDecoder[F, Joke] =
      jsonOf

    implicit val jokeEncoder: Encoder[Joke] = deriveEncoder[Joke]

    implicit def jokeEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Joke] =
      jsonEncoderOf
  }

  final case class JokeError(e: Throwable) extends RuntimeException

  def impl[F[_] : Sync : Monad](config: Config, C: Client[F], LOG: Logger[F], PR: PlayerRepository[F]): Jokes[F] = new Jokes[F] {
    val dsl: Http4sClientDsl[F] = new Http4sClientDsl[F] {}

    import dsl._

    def get: F[Result[Jokes.Joke]] = {
      val x: F[Joke] = C.expect[Joke](GET(uri"https://icanhazdadjoke.com/"))
        .adaptError { case t => JokeError(t) } // Prevent Client Json Decoding Failure Leaking

      val result = for {
        all <- PR.listAll
        _ <- Flow.log(LOG.info(s"Found ${all.size} players"))
        res <- Flow.fromF(x)
        _ <- KafkaProducer.writeToKafka(s"FOUND JOKE: ${res.toString}")
      } yield res

      result.value
    }
  }
}