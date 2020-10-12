package com.mairo.ukl.services

import cats.Monad
import cats.effect.{Sync, Timer}
import com.mairo.ukl.bot.BotCmdProcessor
import com.mairo.ukl.services.impl.TelegramMsgProcessorImpl
import com.mairo.ukl.utils.Flow.Flow

trait TelegramMsgProcessor[F[_]] {

  def processMsg(msg: String): Flow[F, Unit]
}

object TelegramMsgProcessor {
  val notAvailable = "n/a"
  val ERROR = "*ERROR*:"

  def apply[F[_]](implicit ev: TelegramMsgProcessor[F]): TelegramMsgProcessor[F] = ev

  def impl[F[_] : Monad : Sync : Timer](botCmdProcessor: BotCmdProcessor[F]): TelegramMsgProcessor[F] =
    new TelegramMsgProcessorImpl[F](botCmdProcessor: BotCmdProcessor[F])
}
