package com.mairo.ukl.zio.configs

import reactivemongo.api.{ DB, MongoConnection }
import zio.{ Has, Task, ZLayer }

object DbClient {

  val uri = "mongodb://user123:passwd123@host1:27018,host2:27019,host3:27020/somedb"
  type HasDb = Has[DB]

  def connection: Task[MongoConnection] = {
    val driver = new reactivemongo.api.AsyncDriver
    Task.fromFuture { implicit ec =>
      for {
        parsedUri <- MongoConnection.fromString(uri)
        con       <- driver.connect(parsedUri)
      } yield con
    }
  }

  def seasonDb(conn: MongoConnection): Task[DB] =
    Task.fromFuture(ec => conn.database("cata")(ec))

  val live: ZLayer[Any, Throwable, HasDb] =
    (for {
      c  <- connection
      db <- seasonDb(c)
    } yield db).toLayer

}
