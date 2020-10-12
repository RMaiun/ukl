package com.mairo.ukl.services.impl

import cats.Monad
import cats.effect.{Sync, Timer}
import com.mairo.ukl.bot.BotCmdProcessor
import com.mairo.ukl.dtos.AddRoundDto
import com.mairo.ukl.dtos.AddRoundDto._
import com.mairo.ukl.errors.UklException.{InvalidBotRequest, InvalidTelegramCmd}
import com.mairo.ukl.services.TelegramMsgProcessor
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow
import io.circe._
import io.circe.optics.JsonPath._
import io.circe.parser._

class TelegramMsgProcessorImpl[F[_] : Monad : Sync : Timer](botCmdProcessor: BotCmdProcessor[F]) extends TelegramMsgProcessor[F] {
  override def processMsg(msg: String): Flow[F, Unit] = {
    val json: Json = parse(msg).getOrElse(Json.Null)
    val cmdPath = root.cmd.string
    val chatIdPath = root.chatId.string
    val msgIdPath = root.msgId.int
    val dataPath = root.data.json
    val cmdVal = cmdPath.getOption(json)
    val chatIdOpt = chatIdPath.getOption(json)
    val dataOpt = dataPath.getOption(json)
    val msgIdOpt = msgIdPath.getOption(json)

    val maybeResult = for {
      cmd <- cmdVal
      chatId <- chatIdOpt
      msgId <- msgIdOpt
      data <- dataOpt
    } yield {
      cmd match {
        case "listPlayers" =>
          botCmdProcessor.listPlayersCmd(chatId, msgId)
        case "addRound" =>
          for {
            body <- Flow.fromRes(data.as[AddRoundDto])
            result <- botCmdProcessor.addRoundCmd(chatId, msgId, body)
          } yield result
        case _ =>
          Flow.error[F, Unit](InvalidTelegramCmd(cmd))
      }
    }

    maybeResult match {
      case Some(value) => value
      case None =>
        Flow.fromF(Sync[F].delay(InvalidBotRequest(chatIdOpt, cmdVal, dataOpt).printStackTrace()))
    }
  }
}
