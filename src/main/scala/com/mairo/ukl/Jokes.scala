package com.mairo.ukl

import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Monad}
import com.mairo.ukl.dtos.{BotRequestDto, FoundAllPlayersDto}
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.rabbit.RabbitSender
import com.mairo.ukl.repositories.PlayerRepository
import com.mairo.ukl.utils.Flow.Flow
import com.mairo.ukl.utils.ResultOps.Result
import com.mairo.ukl.utils.{Flow, FlowLog}
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

  def impl[F[_] : Sync : Monad : Logger](config: Config, C: Client[F], PR: PlayerRepository[F], RP: RabbitSender[F]): Jokes[F] = new Jokes[F] {
    val dsl: Http4sClientDsl[F] = new Http4sClientDsl[F] {}

    import dsl._

    def get: F[Result[Jokes.Joke]] = {
      val x: F[Either[Throwable, Joke]] = C.expect[Joke](GET(uri"https://icanhazdadjoke.com/"))
        .attempt

      val result = for {
        all <- PR.listAll
        _ <- FlowLog.info(s"Found ${all.size} players")
        res <- Flow(x)
        _ <- publish("1", FoundAllPlayersDto(all))
        _ <- publish("2", FoundAllPlayersDto(all))
      } yield res

      result.value
    }

    private def publish(chatId: String, data: FoundAllPlayersDto): Flow[F, Unit] = {
      import FoundAllPlayersDto._
      import io.circe.syntax._
      val json = data.asJson
      val request = BotRequestDto("listCmdPlayers", chatId, "123", json)
      val strRequest = request.asJson.toString()
      RP.publishString(strRequest, config.rabbit.inputChannel)
    }
  }
}