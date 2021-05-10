package com.mairo.ukl.zio.repositories

import com.mairo.ukl.zio.configs.DbClient.HasDb
import com.mairo.ukl.zio.repositories.interpreters.PlayerRepositoryInterpreter
import reactivemongo.api.bson.BSONDocument
import zio.{ Has, Task, ZLayer }

object PlayerRepository {
  type HasPlayerRepo = Has[PlayerRepository.Service]

  trait Service {
    def listAll(): Task[List[Player]]
    def findPlayers(surnames: List[String]): Task[List[Player]]
    def getPlayer(name: String): Task[Option[Player]]
    def getPlayerByCriteria(criteria: BSONDocument): Task[Option[Player]]
    def savePlayer(p: Player): Task[Player]
    def updatePlayer(p: Player): Task[Player]
    def removeAll(): Task[Int]
  }

  val live: ZLayer[HasDb, Nothing, HasPlayerRepo] =
    ZLayer.fromService(db => new PlayerRepositoryInterpreter(db))
}
