package com.mairo.ukl.processor

import cats.Monad
import com.mairo.ukl.dtos.AddRoundDto._
import com.mairo.ukl.helper.MessageFormatter
import com.mairo.ukl.processor.CommandObjects.BotOutputMessage
import com.mairo.ukl.services.RoundService
import com.mairo.ukl.utils.flow.Flow.Flow

class AddRoundCmdProcessor[F[_] : Monad](roundService: RoundService[F])
  extends CommandProcessor[F] {
  override def commands(): Seq[String] = Seq(AddRoundCmd)

  override def process(input: CommandObjects.BotInputMessage): Flow[F, CommandObjects.BotOutputMessage] = {
    for {
      data <- parse(input.data)
      id <- roundService.saveRound(data)
      msg <- MessageFormatter.formatStoredRoundId(id)
    } yield BotOutputMessage(input.chatId, msg)
  }
}
