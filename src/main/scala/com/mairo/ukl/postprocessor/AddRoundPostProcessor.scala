package com.mairo.ukl.postprocessor

import cats.Monad
import com.mairo.ukl.domains.Player
import com.mairo.ukl.dtos.AddRoundDto._
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.helper.MessageFormatter
import com.mairo.ukl.processor.CommandObjects
import com.mairo.ukl.processor.CommandObjects.BotOutputMessage
import com.mairo.ukl.rabbit.RabbitSender
import com.mairo.ukl.services.PlayerService
import com.mairo.ukl.utils.flow.Flow
import com.mairo.ukl.utils.flow.Flow.Flow

class AddRoundPostProcessor[F[_] : Monad](playerService: PlayerService[F],
                                          rabbitSender: RabbitSender[F])
                                         (implicit config: Config)
  extends PostProcessor[F] {

  override def commands(): Seq[String] = Seq(AddRoundCmd)

  override def postProcess(input: CommandObjects.BotInputMessage): Flow[F, Unit] = {
    for {
      data <- parse(input.data)
      w1 <- playerService.findPlayerByName(data.w1)
      w2 <- playerService.findPlayerByName(data.w2)
      l1 <- playerService.findPlayerByName(data.l1)
      l2 <- playerService.findPlayerByName(data.l2)
      _ <- sendNotification(w1, data.l1.capitalize, data.l2.capitalize, isWinner = true)
      _ <- sendNotification(w2, data.l1.capitalize, data.l2.capitalize, isWinner = true)
      _ <- sendNotification(l1, data.w1.capitalize, data.w2.capitalize, isWinner = false)
      _ <- sendNotification(l2, data.w1.capitalize, data.w2.capitalize, isWinner = false)
    } yield ()
  }

  private def sendNotification(player: Player, op1: String, op2: String, isWinner: Boolean): Flow[F, Unit] = {
    if (config.app.notificationsEnabled) {
      player.tid match {
        case Some(chatId) =>
          for {
            msg <- MessageFormatter.formatPlayerNotification(op1, op2, isWinner)
            _ <- rabbitSender.publish(BotOutputMessage(chatId, msg, msgId()))
          } yield ()
        case None => Flow.unit
      }
    } else {
      Flow.unit
    }
  }
}
