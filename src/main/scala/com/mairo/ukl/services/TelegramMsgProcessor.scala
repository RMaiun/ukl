package com.mairo.ukl.services

import cats.Monad
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.rabbit.RabbitProducer
import com.mairo.ukl.services.impl.TelegramMsgProcessorImpl
import com.mairo.ukl.utils.Flow.Flow

trait TelegramMsgProcessor[F[_]] {

  def processMsg(msg: String): Flow[F, Unit]
}

object TelegramMsgProcessor {
  def apply[F[_]](implicit ev: TelegramMsgProcessor[F]): TelegramMsgProcessor[F] = ev

  def impl[F[_] : Monad](RoundService: RoundService[F],
                         StatisticService: StatisticService[F],
                         PlayerService: PlayerService[F],
                         RabbitProducer: RabbitProducer[F])
                        (implicit config: Config): TelegramMsgProcessor[F] =
    new TelegramMsgProcessorImpl[F](RoundService, StatisticService, PlayerService, RabbitProducer)
}
