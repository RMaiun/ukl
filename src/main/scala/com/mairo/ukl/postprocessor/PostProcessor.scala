package com.mairo.ukl.postprocessor

import cats.Monad
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.processor.CommandObjects.BotInputMessage
import com.mairo.ukl.rabbit.RabbitSender
import com.mairo.ukl.services.PlayerService
import com.mairo.ukl.utils.flow.Flow.Flow
import com.mairo.ukl.utils.{Commands, MsgIdGenerator, ParseSupport}
import io.chrisdavenport.log4cats.Logger

trait PostProcessor[F[_]] extends ParseSupport[F] with Commands with MsgIdGenerator {
  def postProcess(input: BotInputMessage): Flow[F, Unit]
}

object PostProcessor {
  def apply[F[_]](implicit ev: PostProcessor[F]): PostProcessor[F] = ev

  def addRoundPostProcessor[F[_] : Monad : Logger](playerService: PlayerService[F],
                                                   rabbitSender: RabbitSender[F])
                                                  (implicit config: Config): PostProcessor[F] =
    new AddRoundPostProcessor[F](playerService, rabbitSender)

  def subscriptionPostProcessor[F[_] : Monad : Logger](rabbitSender: RabbitSender[F])
                                                      (implicit config: Config): PostProcessor[F] =
    new SubscriptionPostProcessor[F](rabbitSender)

  def allPostProcessors[F[_] : Monad : Logger](playerService: PlayerService[F],
                                               rabbitSender: RabbitSender[F])
                                              (implicit config: Config): Seq[PostProcessor[F]] = {
    Seq(addRoundPostProcessor(playerService, rabbitSender), subscriptionPostProcessor(rabbitSender))
  }
}
