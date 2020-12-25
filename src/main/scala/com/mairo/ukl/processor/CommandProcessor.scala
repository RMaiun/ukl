package com.mairo.ukl.processor

import cats.Monad
import com.mairo.ukl.processor.CommandObjects.{BotInputMessage, BotOutputMessage}
import com.mairo.ukl.services.PlayerService
import com.mairo.ukl.utils.Flow.Flow
import com.mairo.ukl.utils.{Commands, ParseSupport}

trait CommandProcessor[F[_]] extends ParseSupport[F] with Commands {
  def process(input: BotInputMessage): Flow[F, BotOutputMessage]
}

object CommandProcessor {
  def apply[F[_]](implicit ev: CommandProcessor[F]): CommandProcessor[F] = ev

  def listPlayersProcessor[F[_] : Monad](playerService: PlayerService[F]): CommandProcessor[F] = new ListPlayersCmdProcessor[F](playerService)

  def addPlayerProcessor[F[_] : Monad](playerService: PlayerService[F]): CommandProcessor[F] = new AddPlayerCmdProcessor[F](playerService)
}
