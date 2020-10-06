package com.mairo.ukl.services

import cats.Monad
import cats.effect.ConcurrentEffect
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.rabbit.RabbitProducer
import com.mairo.ukl.services.impl.TelegramMsgProcessorImpl

trait TelegramMsgProcessor[F[_]] {

  def processMsg(msg: String): Unit
}

object TelegramMsgProcessor {
  def apply[F[_]](implicit ev: TelegramMsgProcessor[F]): TelegramMsgProcessor[F] = ev

  def impl[F[_] : Monad : ConcurrentEffect](RoundService: RoundService[F],
                                            StatisticService: StatisticService[F],
                                            PlayerService: PlayerService[F],
                                            RabbitProducer: RabbitProducer[F])
                                           (implicit config: Config): TelegramMsgProcessor[F] =
    new TelegramMsgProcessorImpl[F](RoundService, StatisticService, PlayerService, RabbitProducer)
}
