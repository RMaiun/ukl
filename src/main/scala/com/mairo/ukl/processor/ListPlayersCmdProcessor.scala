package com.mairo.ukl.processor

import cats.Monad
import com.mairo.ukl.helper.MessageFormatter
import com.mairo.ukl.processor.CommandObjects.BotOutputMessage
import com.mairo.ukl.services.PlayerService
import com.mairo.ukl.utils.Flow.Flow
import com.mairo.ukl.utils.FlowLog
import io.chrisdavenport.log4cats.Logger

class ListPlayersCmdProcessor[F[_] : Monad : Logger](playerService: PlayerService[F]) extends CommandProcessor[F] {

  override def commands(): Seq[String] = Seq(ListPlayersCmd)

  override def process(input: CommandObjects.BotInputMessage): Flow[F, BotOutputMessage] = {
    for {
      players <- playerService.findAllPlayers
      _ <- FlowLog.info(s"Found ${players.players.size} players")
      msg <- MessageFormatter.formatPlayers(players)
    } yield BotOutputMessage(input.chatId, msg)
  }
}
