package com.mairo.ukl.repositories

import cats.effect.{ContextShift, IO, _}
import com.mairo.ukl.domains.PlayerQueries
import com.mairo.ukl.utils.ConfigProvider
import doobie._
import doobie.util.ExecutionContexts
import org.scalatest.{funsuite, matchers}

class PlayerRepositoryAnalysisTestScalaCheck extends funsuite.AnyFunSuite
  with matchers.must.Matchers
  with doobie.scalatest.IOChecker {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val config: ConfigProvider.Config = ConfigProvider.provideConfig

  override def transactor: doobie.Transactor[IO] = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://localhost:3306/cata?useSSL=false&generateSimpleParameterMetadata=true",
    "root",
    "password",
    Blocker.liftExecutionContext(ExecutionContexts.synchronous))

  test("PlayersQueries.getLastPlayerId Analysis") {
    val query = PlayerQueries.getLastPlayerId
    check(query)
  }
}
