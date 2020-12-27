package com.mairo.ukl.processor

import cats.Monad
import com.mairo.ukl.processor.CommandObjects.{BotInputMessage, BotOutputMessage}
import com.mairo.ukl.services.{PlayerService, RoundService, StatisticService, SubscriptionService}
import com.mairo.ukl.utils.flow.Flow.Flow
import com.mairo.ukl.utils.{Commands, ParseSupport}
import io.chrisdavenport.log4cats.Logger

trait CommandProcessor[F[_]] extends ParseSupport[F] with Commands {
  def process(input: BotInputMessage): Flow[F, BotOutputMessage]
}

object CommandProcessor {
  def apply[F[_]](implicit ev: CommandProcessor[F]): CommandProcessor[F] = ev

  def listPlayersProcessor[F[_] : Monad : Logger](playerService: PlayerService[F]): CommandProcessor[F] = new ListPlayersCmdProcessor[F](playerService)

  def addPlayerProcessor[F[_] : Monad](playerService: PlayerService[F]): CommandProcessor[F] = new AddPlayerCmdProcessor[F](playerService)

  def addRoundProcessor[F[_] : Monad](roundService: RoundService[F]): CommandProcessor[F] = new AddRoundCmdProcessor[F](roundService)

  def lastCmdProcessor[F[_] : Monad](roundService: RoundService[F]): CommandProcessor[F] = new LastCmdProcessor[F](roundService)

  def linkTidCmdProcessor[F[_] : Monad](subscriptionService: SubscriptionService[F]): CommandProcessor[F] = new LinkTidCmdProcessor[F](subscriptionService)

  def statsCmdProcessor[F[_] : Monad](statisticService: StatisticService[F]): CommandProcessor[F] = new StatsCmdProcessor[F](statisticService)

  def subscriptionCmdProcessor[F[_] : Monad](subscriptionService: SubscriptionService[F]): CommandProcessor[F] = new SubscriptionCmdProcessor[F](subscriptionService)

  def allProcessors[F[_] : Monad : Logger](playerService: PlayerService[F],
                                           roundService: RoundService[F],
                                           statisticService: StatisticService[F],
                                           subscriptionService: SubscriptionService[F]): Seq[CommandProcessor[F]] = {
    Seq(addPlayerProcessor(playerService),
      addRoundProcessor(roundService),
      lastCmdProcessor(roundService),
      linkTidCmdProcessor(subscriptionService),
      listPlayersProcessor(playerService),
      statsCmdProcessor(statisticService),
      subscriptionCmdProcessor(subscriptionService)
    )
  }
}
