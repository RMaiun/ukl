package com.mairo.ukl.services.impl

import java.time.LocalDateTime

import cats.Monad
import com.mairo.ukl.domains.{Player, Round, Season}
import com.mairo.ukl.dtos.InternalDto.{RoundPlayerNames, RoundPlayers}
import com.mairo.ukl.dtos._
import com.mairo.ukl.errors.UklException.{PlayersNotFoundException, SamePlayersInRoundException}
import com.mairo.ukl.helper.QuarterCalculator.currentQuarter
import com.mairo.ukl.repositories.RoundRepository
import com.mairo.ukl.services.{PlayerService, RoundService, SeasonService, UserRightsService}
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow

class RoundServiceImpl[F[_] : Monad](PlayerService: PlayerService[F],
                                     SeasonService: SeasonService[F],
                                     RoundRepository: RoundRepository[F],
                                     UserRightsService: UserRightsService[F]) extends RoundService[F] {

  override def findLastRoundsInSeason(dto: FindLastRoundsDto): Flow[F, FoundLastRoundsDto] = {
    for {
      playerMap <- PlayerService.findAllPlayersAsMap
      season <- SeasonService.findSafe(dto.season)
      rounds <- RoundRepository.listLimitedLastRoundsBySeason(season.id, dto.qty)
    } yield {
      val transformedRounds = transformRounds(season, playerMap, rounds)
      FoundLastRoundsDto(transformedRounds)
    }
  }

  override def findAllRounds(seasonName: String): Flow[F, List[FullRound]] = {
    for {
      playerMap <- PlayerService.findAllPlayersAsMap
      season <- SeasonService.findSafe(seasonName)
      rounds <- RoundRepository.listRoundsBySeason(season.id)
    } yield transformRounds(season, playerMap, rounds)
  }

  override def saveRound(dto: AddRoundDto): Flow[F, IdDto] = {
    //todo: add cache for future document
    for {
      _ <- UserRightsService.checkUserIsAdmin(dto.moderator)
      _ <- checkAllPlayersAreDifferent(dto)
      season <- SeasonService.findSafe(currentQuarter)
      data <- PlayerService.findAllPlayers
      roundPlayers <- checkAllPlayersArePresent(RoundPlayerNames(dto.w1, dto.w2, dto.l1, dto.l2), data.players)
      storedId <- RoundRepository.insert(roundPlayers.w1.id, roundPlayers.w2.id, roundPlayers.l1.id, roundPlayers.l2.id, dto.shutout, season.id, LocalDateTime.now())
    } yield IdDto(storedId)
  }

  private def transformRounds(season: Season, players: Map[Long, String], rounds: List[Round]): List[FullRound] = {
    rounds.map(r => FullRound(
      r.winner1, players.getOrElse(r.winner1, "").capitalize,
      r.winner2, players.getOrElse(r.winner2, "").capitalize,
      r.loser1, players.getOrElse(r.loser1, "").capitalize,
      r.loser2, players.getOrElse(r.loser2, "").capitalize,
      r.created, season.name, r.shutout)
    )
      .sortBy(_.created)
  }

  private def checkAllPlayersAreDifferent(dto: AddRoundDto): Flow[F, Unit] = {
    val roundPlayers = List(dto.w1, dto.w2, dto.l1, dto.l2).distinct
    if (roundPlayers.length == 4) {
      Flow.unit
    } else {
      Flow.error(SamePlayersInRoundException())
    }
  }

  private def checkAllPlayersArePresent(playerNames: RoundPlayerNames, allPlayers: List[Player]): Flow[F, RoundPlayers] = {
    val result = for {
      w1 <- allPlayers.find(_.surname == playerNames.w1)
      w2 <- allPlayers.find(_.surname == playerNames.w2)
      l1 <- allPlayers.find(_.surname == playerNames.l1)
      l2 <- allPlayers.find(_.surname == playerNames.l2)
    } yield RoundPlayers(w1, w2, l1, l2)

    result match {
      case Some(value) => Flow.pure(value)
      case None =>
        val roundPlayerNames = List(playerNames.w1, playerNames.w2, playerNames.l1, playerNames.l2)
        val allNames = allPlayers.map(_.surname)
        val notFound = roundPlayerNames.filter(name => !allNames.contains(name))
        Flow.error(PlayersNotFoundException(notFound))
    }
  }
}
