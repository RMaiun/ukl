package com.mairo.ukl.repositories

import java.sql.SQLException

import cats.Monad
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.syntax.either._
import com.mairo.ukl.domains.PlayerDomains.Player
import com.mairo.ukl.domains.PlayerQueries
import com.mairo.ukl.errors.UklException.DbException
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.chrisdavenport.log4cats.Logger

trait PlayerRepository[F[_]] {
  def findAll: Flow[F, List[Player]]

  def findPlayers(surnames: List[String]): Flow[F, List[Player]]

  def getPlayer(name: String): Flow[F, Option[Player]]

  def savePlayer(player: Player): Flow[F, Long]

  def deletePlayerById(id: Long): Flow[F, Long]

  def findLastId: Flow[F, Long]

  def clearTable: Flow[F, Unit]
}

object PlayerRepository {
  def apply[F[_]](implicit ev: PlayerRepository[F]): PlayerRepository[F] = ev


  def impl[F[_] : Logger : Sync : Monad](xa: HikariTransactor[F]): PlayerRepository[F] = new PlayerRepository[F] {
    override def findAll: Flow[F, List[Player]] = {
      val result = PlayerQueries.findAllPlayers
        .to[List]
        .transact(xa)
      Monad[F].map(result)(r => r.asRight[Throwable])
    }

    override def findPlayers(surnames: List[String]): Flow[F, List[Player]] = {
      val surnamesList = NonEmptyList.fromList(surnames)
        .fold(NonEmptyList.of(PlayerQueries.invalidPlayer))(x => x)
      val result = PlayerQueries.findPlayers(surnamesList)
        .to[List]
        .transact(xa)
      Monad[F].map(result)(r => r.asRight[Throwable])
    }

    override def getPlayer(name: String): Flow[F, Option[Player]] = {
      val result = PlayerQueries.getPlayerByName(name)
        .option
        .transact(xa)
      Monad[F].map(result)(r => r.asRight[Throwable])
    }

    override def savePlayer(player: Player): Flow[F, Long] = {
      val result = PlayerQueries.insertPlayer(player.id, player.surname, player.tid, player.cid, player.admin)
        .withUniqueGeneratedKeys[Long]("id")
        .transact(xa)
      Monad[F].map(result.attemptSql)(res => res.leftMap(err => DbException(err)))
    }

    override def deletePlayerById(id: Long): Flow[F, Long] = {
      val result = PlayerQueries.deletePlayerById(id)
        .run
        .transact(xa)
      Monad[F].map(result)(_ => id.asRight[Throwable])
    }

    override def findLastId: Flow[F, Long] = {
      val result = PlayerQueries.getLastPlayerId
        .unique
        .transact(xa)
      Monad[F].map(result)(_.asRight[Throwable])
    }

    override def clearTable: Flow[F, Unit] = {
      val result = PlayerQueries.clearTable
        .run
        .transact(xa)
      Monad[F].map(result)(_ => ().asRight[Throwable])
    }
  }

}
