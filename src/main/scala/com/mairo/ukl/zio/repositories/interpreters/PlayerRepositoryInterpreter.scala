package com.mairo.ukl.zio.repositories.interpreters

import com.mairo.ukl.zio.repositories.{ Player, PlayerRepository }
import reactivemongo.api.{ DB }
import reactivemongo.api.bson.collection.{ BSONCollection }
import reactivemongo.api.bson.{ BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros }
import reactivemongo.api.collections._
import zio.Task

import scala.concurrent.{ ExecutionContext, Future }

class PlayerRepositoryInterpreter(db: DB) extends PlayerRepository.Service {
  implicit val reader: BSONDocumentReader[Player] = Macros.reader[Player]
  implicit val writer: BSONDocumentWriter[Player] = Macros.writer[Player]

  def collection: BSONCollection = db.collection("player")

  override def listAll(): Task[List[Player]] =
    Task.fromFuture(implicit ec => collection.find(BSONDocument.empty).cursor[Player]().collect[List]())

  override def findPlayers(surnames: List[String]): Task[List[Player]] = {
    val query = BSONDocument("surname" -> BSONDocument("$in" -> BSONArray(surnames)))
    Task.fromFuture(implicit ec => collection.find(query).cursor[Player]().collect[List]())
  }

  override def getPlayer(name: String): Task[Option[Player]] = {
    val query = BSONDocument("surname" -> name)
    Task.fromFuture(implicit ec => collection.find(query).one[Player])
  }

  override def getPlayerByCriteria(criteria: BSONDocument): Task[Option[Player]] =
    Task.fromFuture(implicit ec => collection.find(criteria).one[Player])

  override def savePlayer(p: Player): Task[Player] =
    for {
      _ <- Task.fromFuture(implicit ec => collection.insert.one(p))
    } yield p

  override def updatePlayer(p: Player): Task[Player] = {
    val selector = BSONDocument("_id" -> p._id)
    for {
      _ <- Task.fromFuture(implicit ec => collection.update.one(selector, p, upsert = true, multi = false))
    } yield p
  }

  override def removeAll(): Task[Int] = {
    val deleteBuilder = collection.delete(ordered = false)
    for {
      ops <- Task.fromFuture(implicit ec =>
               Future.sequence(Seq(deleteBuilder.element(q = BSONDocument.empty, limit = Some(1), collation = None)))
             )
      res <- Task.fromFuture(implicit ec => deleteBuilder.many(ops))
    } yield res.nModified
  }

  def bulkDelete(personColl: BSONCollection)(implicit ec: ExecutionContext) = {
    val deleteBuilder = personColl.delete(ordered = false)

    val deletes = Future.sequence(
      Seq(
        deleteBuilder.element(
          q = BSONDocument.empty,
          limit = Some(1),
          collation = None
        )
      )
    )

    deletes.flatMap(ops => deleteBuilder.many(ops))
  }

}
