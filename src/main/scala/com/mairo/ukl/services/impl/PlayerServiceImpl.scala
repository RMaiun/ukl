package com.mairo.ukl.services.impl

import cats.Monad
import cats.syntax.either._
import com.mairo.ukl.domains.Player
import com.mairo.ukl.dtos.{AddPlayerDto, FoundAllPlayersDto, IdDto}
import com.mairo.ukl.errors.UklException.{PlayerAlreadyExistsException, PlayerNotFoundException, PlayersNotFoundException}
import com.mairo.ukl.repositories.PlayerRepository
import com.mairo.ukl.services.{PlayerService, UserRightsService}
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow
import com.mairo.ukl.utils.ResultOps.Result

class PlayerServiceImpl[F[_] : Monad](PlayerRepo: PlayerRepository[F],
                                      UserRightsService: UserRightsService[F])
  extends PlayerService[F] {
  override def findAllPlayersAsMap: Flow[F, Map[Long, String]] = {
    PlayerRepo.listAll.map(_.map(p => (p.id, p.surname)).toMap)
  }

  override def findAllPlayers: Flow[F, FoundAllPlayersDto] = {
    PlayerRepo.listAll.map(FoundAllPlayersDto(_))
  }

  override def checkPlayersExist(surnameList: List[String]): Flow[F, List[Player]] = {
    val surnames = surnameList.map(_.toLowerCase())
    for {
      players <- PlayerRepo.findPlayers(surnames)
      checkedPlayers <- Flow.fromRes(prepareCheckedPlayers(players, surnames))
    } yield checkedPlayers
  }

  override def addPlayer(dto: AddPlayerDto): Flow[F, IdDto] = {
    for {
      _ <- UserRightsService.checkUserIsAdmin(dto.moderator)
      _ <- checkPlayerNotExist(dto.surname)
      lastId <- PlayerRepo.findLastId
      player = Player(lastId, dto.surname, dto.tid, None, dto.admin)
      storedId <- PlayerRepo.insert(player)
    } yield IdDto(storedId)
  }

  override def findPlayer(name: String): Flow[F, Player] = {
    PlayerRepo.getByName(name).flatMap(x => Flow.fromOption(x, PlayerNotFoundException(name)))
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
    PlayerRepo.getByName(surname.toLowerCase)
      .flatMap {
        case Some(p) => Flow.error(PlayerAlreadyExistsException(p.id))
        case None => Flow.pure(())
      }
  }
}
