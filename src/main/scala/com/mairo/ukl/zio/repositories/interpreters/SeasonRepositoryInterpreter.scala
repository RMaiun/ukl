package com.mairo.ukl.zio.repositories.interpreters

import com.mairo.ukl.zio.repositories.{Season, SeasonRepository}
import reactivemongo.api.DB
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros}
import zio.Task

class SeasonRepositoryInterpreter(db: DB) extends SeasonRepository.Service {

  implicit val reader: BSONDocumentReader[Season] = Macros.reader[Season]
  implicit val writer: BSONDocumentWriter[Season] = Macros.writer[Season]

  def collection: BSONCollection = db.collection("season")

  override def getSeason(name: String): Task[Option[Season]] = {
    val query = BSONDocument("name" -> name)
    Task.fromFuture(implicit ec => collection.find(query).one[Season])
  }

  override def saveSeason(season: Season): Task[Season] =
    for {
      _ <- Task.fromFuture(implicit ec => collection.insert.one(season))
    } yield season

  override def updateSeason(season: Season): Task[Season] = {
    val selector = BSONDocument("name" -> season.name)
    for {
      _ <- Task.fromFuture(implicit ec => collection.update.one(selector, season, upsert = true, multi = false))
    } yield season
  }

  override def listAll: Task[List[Season]] =
    Task.fromFuture(implicit ec => collection.find(BSONDocument.empty).cursor[Season]().collect[List]())

  override def findFirstSeasonWithoutNotification: Task[Option[Season]] =
    Task.fromFuture(implicit ec => collection.find(BSONDocument("seasonEndNotification" -> None)).one[Season])

}
