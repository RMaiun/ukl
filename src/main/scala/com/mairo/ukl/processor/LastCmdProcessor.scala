package com.mairo.ukl.processor

import cats.Monad
import com.mairo.ukl.dtos.FindLastRoundsDto._
import com.mairo.ukl.helper.MessageFormatter
import com.mairo.ukl.processor.CommandObjects.{BotInputMessage, BotOutputMessage}
import com.mairo.ukl.services.RoundService
import com.mairo.ukl.utils.flow.Flow.Flow

class LastCmdProcessor[F[_] : Monad](roundService: RoundService[F]) extends CommandProcessor[F] {
  override def commands(): Seq[String] = Seq(FindLastRoundsCmd)

  override def process(input: BotInputMessage): Flow[F, BotOutputMessage] = {
    for {
      dto <- parse(input.data)
      res <- roundService.findLastRoundsInSeason(dto)
      msg <- MessageFormatter.formatLastRounds(res)
    } yield BotOutputMessage(input.chatId, msg)
  }

}
