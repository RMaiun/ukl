package com.mairo.ukl.dtos

import com.mairo.ukl.domains.{Player, Round, Season}

object InternalDto {

  case class SPRData(season: Season,
                     player: Map[Long, String],
                     rounds: List[Round])

  case class SPData(season: Season,
                    player: Map[Long, String])

  case class RoundPlayerNames(w1: String,
                              w2: String,
                              l1: String,
                              l2: String)

  case class RoundPlayers(w1: Player,
                          w2: Player,
                          l1: Player,
                          l2: Player)

  case class StatsCalcData(pid: Long,
                           player: String,
                           points: Int,
                           qty: Int = 1)

  case class RatingWithGames(pid: Long,
                             player: String,
                             rating: Int,
                             games: Int)

}
