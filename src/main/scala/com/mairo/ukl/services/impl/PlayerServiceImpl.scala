package com.mairo.ukl.services.impl

import cats.Monad
import cats.syntax.either._
import com.mairo.ukl.domains.Player
import com.mairo.ukl.dtos.{AddPlayerDto, FoundAllPlayersDto, IdDto}
import com.mairo.ukl.errors.UklException.{PlayerAlreadyExistsException, PlayerNotFoundException, PlayersNotFoundException}
import com.mairo.ukl.repositories.PlayerRepository
import com.mairo.ukl.services.PlayerService.{SurnameProp, TidProp}
import com.mairo.ukl.services.{PlayerService, UserRightsService}
import com.mairo.ukl.utils.flow.Flow
import com.mairo.ukl.utils.flow.Flow.Flow
import com.mairo.ukl.utils.flow.ResultOps.Result
import com.mairo.ukl.validations.ValidationSet._
import com.mairo.ukl.validations.Validator

class PlayerServiceImpl[F[_] : Monad](playerRepo: PlayerRepository[F],
                                      userRightsService: UserRightsService[F])
  extends PlayerService[F] {

  override def findAllPlayersAsMap: Flow[F, Map[Long, String]] = {
    playerRepo.listAll.map(_.map(p => (p.id, p.surname)).toMap)
  }

  override def findAllPlayers: Flow[F, FoundAllPlayersDto] = {
    playerRepo.listAll.map(FoundAllPlayersDto(_))
  }

  override def checkPlayersExist(surnameList: List[String]): Flow[F, List[Player]] = {
    val surnames = surnameList.map(_.toLowerCase())
    for {
      players <- playerRepo.findPlayers(surnames)
      checkedPlayers <- Flow.fromRes(prepareCheckedPlayers(players, surnames))
    } yield checkedPlayers
  }

  override def addPlayer(dto: AddPlayerDto): Flow[F, IdDto] = {
    for {
      _ <- Validator.validateDto(dto)
      _ <- userRightsService.checkUserIsAdmin(dto.moderator)
      _ <- checkPlayerNotExist(dto.surname)
      lastId <- playerRepo.findLastId
      player = Player(lastId, dto.surname, dto.tid, dto.admin, notificationsEnabled = false)
      storedId <- playerRepo.insert(player)
    } yield IdDto(storedId)
  }

  override def findPlayerByName(name: String): Flow[F, Player] = {
    playerRepo.getByName(name)
      .flatMap(x => Flow.fromOption(x, PlayerNotFoundException(SurnameProp, name)))
  }


  override def findPlayerByTid(tid: String): Flow[F, Player] = {
    playerRepo.getByTid(tid)
      .flatMap(x => Flow.fromOption(x, PlayerNotFoundException(TidProp, tid)))

  }

  private def prepareCheckedPlayers(players: List[Player], surnames: List[String]): Result[List[Player]] = {
    if (players.size == surnames.size) {
      players.asRight[Throwable]
    } else {
      val foundSurnames = players.map(_.surname)
      val missedPlayers = surnames.filter(s => foundSurnames.contains(s))
      PlayersNotFoundException(missedPlayers).asLeft[List[Player]]
    }
  }

  private def checkPlayerNotExist(surname: String): Flow[F, Unit] = {
    playerRepo.getByName(surname.toLowerCase)
      .flatMap {
        case Some(p) => Flow.error(PlayerAlreadyExistsException(p.id))
        case None => Flow.pure(())
      }
  }

  override def enableNotifications(surname: String, tid: String): Flow[F, Player] = {
    for {
      foundPlayer <- findPlayerByName(surname)
      updPlayer <- playerRepo.update(foundPlayer.copy(tid = Some(tid), notificationsEnabled = true))
    } yield updPlayer
  }

  override def updatePlayer(player: Player): Flow[F, Player] = {
    for {
      _ <- playerRepo.getByName(player.surname)
      updPlayer <- playerRepo.update(player)
    } yield updPlayer
  }
}
