package com.mairo.ukl.services

import cats.Monad
import cats.syntax.either._
import com.mairo.ukl.domains.Player.Player
import com.mairo.ukl.errors.UklException.InvalidUserRightsException
import com.mairo.ukl.repositories.PlayerRepository
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow

trait UserRightsService[F[_]] {
  def checkUserIsAdmin(tid: String): Flow[F, Player]
}

object UserRightsService {
  def apply[F[_]](implicit ev: UserRightsService[F]): UserRightsService[F] = ev

  def impl[F[_] : Monad](PlayerRepo: PlayerRepository[F]): UserRightsService[F] = new UserRightsService[F] {
    override def checkUserIsAdmin(tid: String): Flow[F, Player] = {
      for {
        players <- PlayerRepo.listAll
        trustedPlayer <- checkAdminPermissions(players, tid)
      } yield trustedPlayer
    }

    private def checkAdminPermissions(players: List[Player], tid: String): Flow[F, Player] = {
      players.find(p => p.tid.contains(tid) && p.admin) match {
        case Some(value) => Flow.fromFRes(Monad[F].pure(value.asRight[Throwable]))
        case None => Flow.error(InvalidUserRightsException())
      }

    }
  }
}
