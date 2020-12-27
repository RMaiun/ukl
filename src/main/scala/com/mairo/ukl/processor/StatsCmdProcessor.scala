package com.mairo.ukl.processor

import cats.Monad
import com.mairo.ukl.dtos.SeasonDto
import com.mairo.ukl.dtos.SeasonDto._
import com.mairo.ukl.helper.MessageFormatter
import com.mairo.ukl.processor.CommandObjects.{BotInputMessage, BotOutputMessage}
import com.mairo.ukl.services.StatisticService
import com.mairo.ukl.utils.flow.Flow.Flow

class StatsCmdProcessor[F[_] : Monad](statisticService: StatisticService[F]) extends CommandProcessor[F] {

  override def commands(): Seq[String] = Seq(ShortStatsCmd)


  override def process(input: BotInputMessage): Flow[F, BotOutputMessage] = {
    for {
      dto <- parse(input.data)
      shortStats <- statisticService.seasonShortInfoStatistics(dto)
      msg <- MessageFormatter.formatShortInfoStats(shortStats)
    } yield BotOutputMessage(input.chatId, msg)
  }

}
