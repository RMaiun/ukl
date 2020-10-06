package com.mairo.ukl.services.impl

import cats.Monad
import cats.effect.ConcurrentEffect
import cats.syntax.either._
import com.mairo.ukl.dtos.BotResponse
import com.mairo.ukl.errors.UklException.{InvalidBotRequest, InvalidTelegramCmd}
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.helper.MessageFormatter
import com.mairo.ukl.rabbit.RabbitProducer
import com.mairo.ukl.services.{PlayerService, RoundService, StatisticService, TelegramMsgProcessor}
import com.mairo.ukl.utils.ResultOps
import io.circe._
import io.circe.optics.JsonPath._
import io.circe.parser._

case class RequestData(chatId: String, cmd: String, result: String)

class TelegramMsgProcessorImpl[F[_] : Monad : ConcurrentEffect](RoundService: RoundService[F],
                                                                StatisticService: StatisticService[F],
                                                                PlayerService: PlayerService[F],
                                                                RabbitProducer: RabbitProducer[F])
                                                               (implicit config: Config) extends TelegramMsgProcessor[F] {
  override def processMsg(msg: String): Unit = {
    val json: Json = parse(msg).getOrElse(Json.Null)
    val cmdPath = root.cmd.string
    val chatIdPath = root.chatId.string
    val dataPath = root.data.json
    val cmdVal = cmdPath.getOption(json)
    val chatIdVal = chatIdPath.getOption(json)
    val dataVal = dataPath.getOption(json)

    val maybeResult = for {
      cmd <- cmdVal
      chatId <- chatIdVal
      data <- dataVal
    } yield {
      cmd match {
        case "listPlayers" =>
          listPlayers(chatId, cmd)
        case _ =>
          Monad[F].pure(ResultOps.error[RequestData](InvalidTelegramCmd(cmd)))
      }
    }
    if (maybeResult.isEmpty) {
      InvalidBotRequest(chatIdVal, cmdVal, dataVal).printStackTrace()
    }
  }

  private def listPlayers(chatId: String, cmd: String): Unit = {

    val action = for {
      players <- PlayerService.findAllPlayers
      result <- MessageFormatter.formatPlayers(players)
    } yield {
      RequestData(chatId, cmd, result)
    }

    val b = Monad[F].flatMap(action.value) {
      case Left(err) =>
        println("err")
        Monad[F].map(RabbitProducer.publish(BotResponse(chatId, err.getMessage), config.rabbit.errorChannel))(_ => ResultOps.error[RequestData](err))
      case Right(x) =>
        println("ok")
        Monad[F].map(RabbitProducer.publish(BotResponse(chatId, x.result), config.rabbit.outputChannel))(_ => x.asRight[Throwable])
    }
    ConcurrentEffect[F].toIO(b).unsafeRunAsyncAndForget()
  }
}
