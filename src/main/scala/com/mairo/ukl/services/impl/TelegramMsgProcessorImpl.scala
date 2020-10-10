package com.mairo.ukl.services.impl

import java.util.concurrent.TimeUnit

import cats.Monad
import cats.effect.{Sync, Timer}
import com.mairo.ukl.bot.BotCmdProcessor
import com.mairo.ukl.errors.UklException.{InvalidBotRequest, InvalidTelegramCmd}
import com.mairo.ukl.services.TelegramMsgProcessor
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow
import io.circe._
import io.circe.optics.JsonPath._
import io.circe.parser._
import cats.syntax.flatMap._

import scala.concurrent.duration.FiniteDuration

class TelegramMsgProcessorImpl[F[_] : Monad : Sync:Timer](botCmdProcessor: BotCmdProcessor[F]) extends TelegramMsgProcessor[F] {
  override def processMsg(msg: String): Flow[F, Unit] = {
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
      val x = chatId match {
        case "1" =>  Timer[F].sleep(FiniteDuration(3000, TimeUnit.MILLISECONDS)) >> Sync[F].delay(println("1"))
        case "2" =>  Sync[F].delay(println("2"))
      }
      Flow.fromF(x)
//      cmd match {
//        case "listPlayers" =>
//          botCmdProcessor.listPlayersCmd(chatId)
//        case _ =>
//          Flow.error[F, Unit](InvalidTelegramCmd(cmd))
//      }
    }

    maybeResult match {
      case Some(value) => value
      case None =>
        Flow.fromF(Sync[F].delay(InvalidBotRequest(chatIdVal, cmdVal, dataVal).printStackTrace()))
    }
  }
}
