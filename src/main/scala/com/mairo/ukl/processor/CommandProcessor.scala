package com.mairo.ukl.processor

import cats.Monad
import com.mairo.ukl.processor.CommandObjects.{BotInputMessage, BotOutputMessage}
import com.mairo.ukl.services.PlayerService
import com.mairo.ukl.utils.Flow.Flow
import com.mairo.ukl.utils.{Commands, ParseSupport}
import io.chrisdavenport.log4cats.Logger

trait CommandProcessor[F[_]] extends ParseSupport[F] with Commands {
  def process(input: BotInputMessage): Flow[F, BotOutputMessage]
}

object CommandProcessor {
  def apply[F[_]](implicit ev: CommandProcessor[F]): CommandProcessor[F] = ev

  def listPlayersProcessor[F[_] : Monad : Logger](playerService: PlayerService[F]): CommandProcessor[F] = new ListPlayersCmdProcessor[F](playerService)

  def addPlayerProcessor[F[_] : Monad](playerService: PlayerService[F]): CommandProcessor[F] = new AddPlayerCmdProcessor[F](playerService)

  def allProcessors[F[_] : Monad : Logger](playerService: PlayerService[F]): Seq[CommandProcessor[F]] = {
    Seq(listPlayersProcessor(playerService), addPlayerProcessor(playerService))
  }
}
