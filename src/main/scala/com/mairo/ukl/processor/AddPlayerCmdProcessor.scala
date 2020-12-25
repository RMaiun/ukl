package com.mairo.ukl.processor

import cats.Monad
import com.mairo.ukl.dtos.AddPlayerDto
import com.mairo.ukl.dtos.AddPlayerDto._
import com.mairo.ukl.helper.MessageFormatter
import com.mairo.ukl.processor.CommandObjects.{BotInputMessage, BotOutputMessage}
import com.mairo.ukl.services.PlayerService
import com.mairo.ukl.utils.Flow.Flow

class AddPlayerCmdProcessor[F[_] : Monad](playerService: PlayerService[F]) extends CommandProcessor[F] {

  override def commands(): Seq[String] = Seq(AddPlayerCmd)

  override def process(input: BotInputMessage): Flow[F, BotOutputMessage] = {
    for {
      dto <- parse[AddPlayerDto](input.data)
      id <- playerService.addPlayer(dto)
      msg <- MessageFormatter.formatStoredId(id)
    } yield BotOutputMessage(input.chatId, msg)
  }

}
