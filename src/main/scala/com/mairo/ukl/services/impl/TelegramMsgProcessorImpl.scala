package com.mairo.ukl.services.impl

import cats.Monad
import cats.effect.{ConcurrentEffect, Sync}
import com.mairo.ukl.bot.BotCmdProcessor
import com.mairo.ukl.errors.UklException.{InvalidBotRequest, InvalidTelegramCmd}
import com.mairo.ukl.services.TelegramMsgProcessor
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow
import io.circe._
import io.circe.optics.JsonPath._
import io.circe.parser._

case class RequestData(chatId: String, cmd: String, result: String)

class TelegramMsgProcessorImpl[F[_] : Monad : ConcurrentEffect](botCmdProcessor: BotCmdProcessor[F]) extends TelegramMsgProcessor[F] {
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
      cmd match {
        case "listPlayers" =>
          botCmdProcessor.listPlayersCmd(chatId)
        case _ =>
          Flow.error[F, Unit](InvalidTelegramCmd(cmd))
      }
    }

    maybeResult match {
      case Some(value) => value
      case None =>
        Flow.fromF(Sync[F].delay(InvalidBotRequest(chatIdVal, cmdVal, dataVal).printStackTrace()))
    }
  }
}
