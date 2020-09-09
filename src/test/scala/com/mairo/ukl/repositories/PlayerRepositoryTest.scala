package com.mairo.ukl.repositories

import cats.effect.{ContextShift, IO}
import com.mairo.ukl.domains.PlayerDomains.Player
import com.mairo.ukl.utils.Flow.Result
import com.mairo.ukl.utils.{ConfigProvider, TransactorProvider}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterEach, Inside}

class PlayerRepositoryTest extends AnyFlatSpec
  with Matchers
  with BeforeAndAfterEach
  with Inside {
  implicit def unsafeLogger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  val testPid = 999
  val testName = "TestName"
  val config: ConfigProvider.Config = ConfigProvider.provideConfig
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val transactor: HikariTransactor[IO] = TransactorProvider.hikariTransactor[IO](config, allowPublicKeyRetrieval = true)
  val playerRepo: PlayerRepository[IO] = PlayerRepository.impl[IO](transactor)

  "PlayerRepository" should "insert new player into mysql" in {
    val result: Result[Long] = createUser(testPid, testName)
    result.isRight should be(true)
    result.getOrElse(fail("either was not Right!")) shouldBe 999
    val foundPlayer = playerRepo.getPlayer(testName).unsafeRunSync()
    foundPlayer.isRight should be(true)
    val foundResult = foundPlayer.getOrElse(fail("either was not Right!"))
    foundResult.isDefined should be(true)
    foundResult.get.id shouldBe testPid
  }

  it should "throw exception on duplicate player id" in {
    val result: Result[Long] = createUser(testPid, testName)
    val expectedError = createUser(testPid, testName)
    expectedError.isLeft should be(true)
    inside(expectedError) {
      case Left(err) =>
        err.getMessage should include("Duplicate")
      case Right(_) => fail("DbException is expected")
    }
  }

  it should "find users by surnames" in {
    createUser(testPid, testName)
    createUser(100, "Test2")
    createUser(101, "Test3")
    val result = playerRepo.findPlayers(List(testName, "Test2")).unsafeRunSync()
    result.isRight should be(true)
    result.getOrElse(fail("either was not Right!")).size shouldBe 2
  }

  it should "clear table" in {
    createUser(testPid, testName)
    createUser(100, "Test2")
    createUser(101, "Test3")
    playerRepo.clearTable.unsafeRunSync()
    val result = playerRepo.findPlayers(List(testName)).unsafeRunSync()
    result.isRight should be(true)
    result.getOrElse(fail("either was not Right!")).size shouldBe 0
  }

  private def createUser(id: Long, name: String): Result[Long] = {
    val player = Player(id, name, Some("9z10y"), None, admin = false)
    playerRepo.savePlayer(player).unsafeRunSync()
  }

  override protected def afterEach(): Unit = {
    playerRepo.clearTable.unsafeRunSync()
  }
}
