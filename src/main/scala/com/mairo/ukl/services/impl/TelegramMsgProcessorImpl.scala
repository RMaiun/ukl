package com.mairo.ukl.services.impl

import cats.Monad
import com.mairo.ukl.errors.UklException.{InvalidBotRequest, InvalidTelegramCmd}
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.helper.MessageFormatter
import com.mairo.ukl.rabbit.RabbitProducer
import com.mairo.ukl.services.{PlayerService, RoundService, StatisticService, TelegramMsgProcessor}
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow
import io.circe._
import io.circe.optics.JsonPath._
import io.circe.parser._


class TelegramMsgProcessorImpl[F[_] : Monad](RoundService: RoundService[F],
                                             StatisticService: StatisticService[F],
                                             PlayerService: PlayerService[F],
                                             RabbitProducer: RabbitProducer[F])
                                            (implicit config: Config) extends TelegramMsgProcessor[F] {
  override def processMsg(msg: String): Flow[F, Unit] = {
    val json: Json = parse(msg).getOrElse(Json.Null)
    val cmdPath = root.cmd.string
    val chatIdPath = root.chatId.string
    val dataPath = root.data.json
    val cmdVal = cmdPath.getOption(json)
    val chatIdVal = chatIdPath.getOption(json)
    val dataVal = dataPath.getOption(json)

    val maybeResult: Option[Flow[F, String]] = for {
      cmd <- cmdVal
      chatId <- chatIdVal
      data <- dataVal
    } yield {
      cmd match {
        case "listPlayers" =>
          for {
            players <- PlayerService.findAllPlayers
            result <- MessageFormatter.formatPlayers(players)
          } yield result
        case _ => Flow.error[F, String](InvalidTelegramCmd(cmd))
      }
    }

    val processedResult = maybeResult.getOrElse(Flow.error(InvalidBotRequest(chatIdVal, cmdVal, dataVal)))

    Flow {
      Monad[F].flatMap(processedResult.value)(e => RabbitProducer.publish(e, config.rabbit.outputChannel))
    }
  }
}
