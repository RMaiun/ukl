package com.mairo.ukl.zio.configs

import reactivemongo.api.{ DB, MongoConnection }
import zio.{ Has, Task, ZLayer }

object DbClient {

  type HasDb = Has[DB]

  def connection(uri: String): Task[MongoConnection] = {
    val driver = new reactivemongo.api.AsyncDriver()
    Task.fromFuture { implicit ec =>
      for {
        parsedUri <- MongoConnection.fromString(uri)
        con       <- driver.connect(parsedUri)
      } yield con
    }
  }

  def seasonDb(conn: MongoConnection): Task[DB] =
    Task.fromFuture(ec => conn.database("cata")(ec))

  val live: ZLayer[HasAllConfigs, Throwable, HasDb] =
    ZLayer.fromFunctionM(cfg =>
      for {
        c  <- connection(cfg.get.app.mongoUrl)
        db <- seasonDb(c)
      } yield db
    )

}
