package com.mairo.ukl.repositories

import cats.Monad
import cats.data.NonEmptyList
import cats.effect.Sync
import com.mairo.ukl.domains.PlayerDomains.Player
import com.mairo.ukl.domains.PlayerQueries
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.chrisdavenport.log4cats.Logger

trait PlayerRepository[F[_]] extends GenericRepository[F, Player] {

  def findPlayers(surnames: List[String]): Flow[F, List[Player]]

  def getByName(name: String): Flow[F, Option[Player]]

  def findLastId: Flow[F, Long]
}

object PlayerRepository {

  import GenericRepository._

  def apply[F[_]](implicit ev: PlayerRepository[F]): PlayerRepository[F] = ev


  def impl[F[_] : Logger : Sync : Monad](xa: HikariTransactor[F]): PlayerRepository[F] = new PlayerRepository[F] {
    override def listAll: Flow[F, List[Player]] = {
      val result = PlayerQueries.findAllPlayers
        .to[List]
        .transact(xa)
        .attemptSql
        .adaptError
      Flow.fromFResult(result)
    }

    override def findPlayers(surnames: List[String]): Flow[F, List[Player]] = {
      val surnamesList = NonEmptyList.fromList(surnames)
        .fold(NonEmptyList.of(PlayerQueries.invalidPlayer))(x => x)
      val result = PlayerQueries.findPlayers(surnamesList)
        .to[List]
        .transact(xa)
        .attemptSql
        .adaptError
      Flow.fromFResult(result)
    }

    override def getById(id: Long): Flow[F, Option[Player]] = {
      val result = PlayerQueries.getPlayerById(id)
        .option
        .transact(xa)
        .attemptSql
        .adaptError
      Flow.fromFResult(result)
    }

    override def getByName(name: String): Flow[F, Option[Player]] = {
      val result = PlayerQueries.getPlayerByName(name)
        .option
        .transact(xa)
        .attemptSql
        .adaptError
      Flow.fromFResult(result)
    }

    override def insert(player: Player): Flow[F, Long] = {
      val result = PlayerQueries.insertPlayer(player.id, player.surname, player.tid, player.cid, player.admin)
        .withUniqueGeneratedKeys[Long]("id")
        .transact(xa)
        .attemptSql
        .adaptError
      Flow.fromFResult(result)
    }

    override def update(data: Player): Flow[F, Player] = {
      val result = PlayerQueries.updatePlayer(data)
        .run
        .transact(xa)
        .attemptSql
        .adaptError
      Flow.fromFResult(Monad[F].map(result)(e => e.map(v => data)))
    }

    override def deleteById(id: Long): Flow[F, Unit] = {
      val result = PlayerQueries.deletePlayerById(id)
        .run
        .transact(xa)
        .attemptSql
        .adaptError
      Flow.fromFResult(Monad[F].map(result)(_.map(_ => ())))
    }

    override def findLastId: Flow[F, Long] = {
      val result = PlayerQueries.getLastPlayerId
        .unique
        .transact(xa)
        .attemptSql
        .adaptError
      Flow.fromFResult(result)
    }

    override def clearTable: Flow[F, Unit] = {
      val result = PlayerQueries.clearTable
        .run
        .transact(xa)
        .attemptSql
        .adaptError
      Flow.fromF(Monad[F].map(result)(_ => ()))
    }
  }

}
