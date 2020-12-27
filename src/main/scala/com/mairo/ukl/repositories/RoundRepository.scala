package com.mairo.ukl.repositories

import java.time.LocalDateTime

import cats.Monad
import cats.effect.Sync
import com.mairo.ukl.domains.Round
import com.mairo.ukl.domains.queries.RoundQueries
import com.mairo.ukl.utils.flow
import com.mairo.ukl.utils.flow.Flow.Flow
import com.mairo.ukl.utils.flow.Flow
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.chrisdavenport.log4cats.Logger

trait RoundRepository[F[_]] extends GenericRepository[F, Round] {
  def listRoundsBySeason(seasonId: Long): Flow[F, List[Round]]

  def listLimitedLastRoundsBySeason(seasonId: Long, limit: Int): Flow[F, List[Round]]

  def insert(winner1: Long,
             winner2: Long,
             loser1: Long,
             loser2: Long,
             shutout: Boolean,
             seasonId: Long,
             created: LocalDateTime): Flow[F, Long]

  def update(data: Round): Flow[F, Round]
}

object RoundRepository {

  import GenericRepository._

  def apply[F[_]](implicit ev: SeasonRepository[F]): SeasonRepository[F] = ev

  def impl[F[_] : Logger : Sync : Monad](xa: HikariTransactor[F]): RoundRepository[F] = new RoundRepository[F] {

    override def insert(winner1: Long, winner2: Long, loser1: Long, loser2: Long, shutout: Boolean, seasonId: Long, created: LocalDateTime): Flow[F, Long] = {
      val result = RoundQueries.insertRound(winner1, winner2, loser1, loser2, shutout, seasonId, created)
        .withUniqueGeneratedKeys[Long]("id")
        .transact(xa)
        .attemptSql
        .adaptError
      flow.Flow(result)
    }

    override def update(data: Round): Flow[F, Round] = {
      val result = RoundQueries.updateRound(data)
        .withUniqueGeneratedKeys[Long]("id")
        .transact(xa)
        .attemptSql
        .adaptError
      Flow(Monad[F].map(result)(e => e.map(v => data)))
    }

    override def listRoundsBySeason(seasonId: Long): Flow[F, List[Round]] = {
      val result = RoundQueries.listRoundsBySeason(seasonId)
        .to[List]
        .transact(xa)
        .attemptSql
        .adaptError
      flow.Flow(result)
    }

    override def listLimitedLastRoundsBySeason(seasonId: Long, limit: Int): Flow[F, List[Round]] = {
      val result = RoundQueries.limitedLastRoundsBySeason(seasonId, limit)
        .to[List]
        .transact(xa)
        .attemptSql
        .adaptError
      flow.Flow(result)
    }

    override def listAll: Flow[F, List[Round]] = {
      val result = RoundQueries.findAllRounds
        .to[List]
        .transact(xa)
        .attemptSql
        .adaptError
      flow.Flow(result)
    }

    override def getById(id: Long): Flow[F, Option[Round]] = {
      val result = RoundQueries.getRoundById(id)
        .option
        .transact(xa)
        .attemptSql
        .adaptError
      flow.Flow(result)
    }

    override def deleteById(id: Long): Flow[F, Long] = {
      val result = RoundQueries.deleteRoundById(id)
        .run
        .transact(xa)
        .attemptSql
        .adaptError
      Flow(Monad[F].map(result)(_.map(_ => id)))
    }

    override def clearTable: Flow[F, Int] = {
      val result = RoundQueries.clearTable
        .run
        .transact(xa)
        .attemptSql
        .adaptError
      flow.Flow(result)
    }
  }
}
