package com.mairo.ukl.zio.repositories

import com.mairo.ukl.zio.configs.DbClient.HasDb
import zio.{ Has, RIO, Task, ZIO, ZLayer }

object SeasonRepository {
  type HasSeasonRepo = Has[SeasonRepository.Service]

  trait Service {
    def getSeason(name: String): Task[Option[Season]]

    def saveSeason(season: Season): Task[Season]

    def updateSeason(season: Season): Task[Season]

    def listAll: Task[List[Season]]

    def findFirstSeasonWithoutNotification: Task[Option[Season]]
  }

  val live: ZLayer[HasDb, Nothing, HasSeasonRepo] = ZLayer.fromService(db => new SeasonRepositoryService(db))

  def getSeason(name: String): RIO[HasSeasonRepo, Option[Season]] = ZIO.accessM(_.get.getSeason(name))

  def saveSeason(season: Season): RIO[HasSeasonRepo, Season] = ZIO.accessM(_.get.saveSeason(season))

  def updateSeason(season: Season): RIO[HasSeasonRepo, Season] = ZIO.accessM(_.get.updateSeason(season))

  def listAll: RIO[HasSeasonRepo, List[Season]] = ZIO.accessM(_.get.listAll)

  def findFirstSeasonWithoutNotification: RIO[HasSeasonRepo, Option[Season]] =
    ZIO.accessM(_.get.findFirstSeasonWithoutNotification)

}
