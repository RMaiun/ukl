package com.mairo.ukl.processor

import cats.Monad
import com.mairo.ukl.dtos.SubscriptionActionDto._
import com.mairo.ukl.processor.CommandObjects.BotOutputMessage
import com.mairo.ukl.services.SubscriptionService
import com.mairo.ukl.utils.flow.Flow.Flow

class SubscriptionCmdProcessor[F[_] : Monad](subscriptionService: SubscriptionService[F]) extends CommandProcessor[F] {

  override def commands(): Seq[String] = Seq(SubscribeCmd, UnsubscribeCmd)


  override def process(input: CommandObjects.BotInputMessage): Flow[F, CommandObjects.BotOutputMessage] = {
    for {
      dto <- parse(input.data)
      sr <- subscriptionService.updateSubscriptionsStatus(dto)
    } yield {
      val action = if (sr.notificationsEnabled) "enabled" else "disabled"
      val msg = s"```Notifications were $action```"
      BotOutputMessage(input.chatId, msg)
    }
  }
}
