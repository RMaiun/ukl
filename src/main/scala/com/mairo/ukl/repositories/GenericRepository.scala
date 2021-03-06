package com.mairo.ukl.repositories

import java.sql.SQLException

import cats.Monad
import com.mairo.ukl.errors.UklException.DbException
import com.mairo.ukl.utils.Flow.{Flow, Result}

trait GenericRepository[F[_], T] {
  def listAll: Flow[F, List[T]]

  def getById(id: Long): Flow[F, Option[T]]

  def insert(data: T): Flow[F, Long]

  def update(data: T): Flow[F, T]

  def deleteById(id: Long): Flow[F, Unit]

  def clearTable: Flow[F, Unit]
}

object GenericRepository {

  implicit class SqlErrorFormer[F[_] : Monad, T](fa: F[Either[SQLException, T]]) {
    def adaptError: F[Result[T]] = {
      Monad[F].map(fa)(e => e.left.map(err => DbException(err)))
    }
  }

}
