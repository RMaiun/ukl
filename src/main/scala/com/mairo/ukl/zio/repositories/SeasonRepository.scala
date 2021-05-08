package com.mairo.ukl.zio.repositories

import com.mairo.ukl.zio.configs.DbClient.HasDb
import zio.{ Has, Task, ZLayer }

object SeasonRepository {
  type HasSeasonRepo = Has[SeasonRepository.Service]

  trait Service {
    def getSeason(name: String): Task[Season]

    def saveSeason(season: Season): Task[Season]

    def updateSeason(season: Season): Task[Season]

    def listAll: Task[List[Season]]

    def findFirstSeasonWithoutNotification: Task[Option[Season]]
  }

  val live: ZLayer[HasDb, Nothing, HasSeasonRepo] = ZLayer.fromService(db => new SeasonRepositoryService(db))
}
