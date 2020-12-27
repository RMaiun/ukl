package com.mairo.ukl.postprocessor

import cats.Monad
import com.mairo.ukl.dtos.LinkTidDto
import com.mairo.ukl.dtos.LinkTidDto._
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.processor.CommandObjects
import com.mairo.ukl.processor.CommandObjects.BotOutputMessage
import com.mairo.ukl.rabbit.RabbitSender
import com.mairo.ukl.utils.flow.Flow
import com.mairo.ukl.utils.flow.Flow.Flow

class SubscriptionPostProcessor[F[_] : Monad](rabbitSender: RabbitSender[F])
                                             (implicit config: Config)
  extends PostProcessor[F] {
  override def commands(): Seq[String] = Seq(LinkTidCmd)


  override def postProcess(input: CommandObjects.BotInputMessage): Flow[F, Unit] = {
    for {
      data <- parse(input.data)
      msg = "```You was participated for notifications```"
      _ <- sendNotification(BotOutputMessage(data.tid, msg, msgId()))
    } yield ()
  }

  private def sendNotification(output: BotOutputMessage): Flow[F, Unit] = {
    if (config.app.notificationsEnabled) {
      rabbitSender.publish(output)
    } else Flow.unit
  }
}
