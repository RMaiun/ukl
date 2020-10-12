package com.mairo.ukl.services

import cats.Monad
import com.mairo.ukl.domains.Player
import com.mairo.ukl.dtos.{AddPlayerDto, FoundAllPlayersDto, IdDto}
import com.mairo.ukl.repositories.PlayerRepository
import com.mairo.ukl.services.impl.PlayerServiceImpl
import com.mairo.ukl.utils.Flow.Flow

trait PlayerService[F[_]] {
  def findAllPlayersAsMap: Flow[F, Map[Long, String]]

  def findAllPlayers: Flow[F, FoundAllPlayersDto]

  def checkPlayersExist(surnameList: List[String]): Flow[F, List[Player]]

  def addPlayer(dto: AddPlayerDto): Flow[F, IdDto]

  def findPlayer(name:String):Flow[F,Player]
}

object PlayerService {
  def apply[F[_]](implicit ev: PlayerService[F]): PlayerService[F] = ev

  def impl[F[_] : Monad](PR: PlayerRepository[F], URS: UserRightsService[F]) =
    new PlayerServiceImpl[F](PR, URS)


}
