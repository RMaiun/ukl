package com.mairo.ukl.processor

import cats.Monad
import com.mairo.ukl.dtos.LinkTidDto._
import com.mairo.ukl.processor.CommandObjects.BotOutputMessage
import com.mairo.ukl.services.SubscriptionService
import com.mairo.ukl.utils.flow.Flow.Flow

class LinkTidCmdProcessor[F[_] : Monad](subscriptionService: SubscriptionService[F]) extends CommandProcessor[F] {
  override def commands(): Seq[String] = Seq(LinkTidCmd)

  override def process(input: CommandObjects.BotInputMessage): Flow[F, CommandObjects.BotOutputMessage] = {
    for {
      dto <- parse(input.data)
      sr <- subscriptionService.linkTidForPlayer(dto)
      msg = s"```Notifications were linked for ${sr.subscribedSurname}````"
    } yield BotOutputMessage(input.chatId, msg)
  }

}
