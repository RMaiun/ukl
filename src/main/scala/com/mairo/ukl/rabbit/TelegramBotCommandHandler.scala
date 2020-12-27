package com.mairo.ukl.rabbit

import cats.Monad
import cats.effect.{Sync, Timer}
import com.mairo.ukl.postprocessor.PostProcessor
import com.mairo.ukl.processor.CommandProcessor
import com.mairo.ukl.services.impl.TelegramBotCommandHandlerImpl
import com.mairo.ukl.utils.flow.Flow.Flow

trait TelegramBotCommandHandler[F[_]] {
  def handleCmd(msg: String): Flow[F, Unit]
}

object TelegramBotCommandHandler {
  def apply[F[_]](implicit ev: TelegramBotCommandHandler[F]): TelegramBotCommandHandler[F] = ev

  def impl[F[_] : Monad : Sync : Timer](processors: Seq[CommandProcessor[F]],
                                        postProcessors: Seq[PostProcessor[F]],
                                        rabbitSender: RabbitSender[F])
  = new TelegramBotCommandHandlerImpl[F](processors, postProcessors, rabbitSender)
}
