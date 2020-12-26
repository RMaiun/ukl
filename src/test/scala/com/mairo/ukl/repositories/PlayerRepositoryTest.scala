package com.mairo.ukl.repositories

import cats.effect.{ContextShift, IO}
import com.mairo.ukl.domains.Player
import com.mairo.ukl.helper.{ConfigProvider, TransactorProvider}
import com.mairo.ukl.services.PlayerService.SurnameProp
import com.mairo.ukl.utils.ResultOps.Result
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

  "PlayerRepository" should "insert new player into db" in {
    val result: Result[Long] = createPlayer(testPid, testName)
    result.isRight should be(true)
    result.getOrElse(fail("either was not Right!")) shouldBe 999
    val foundPlayer = playerRepo.getByName(testName).value.unsafeRunSync()
    foundPlayer.isRight should be(true)
    val foundResult = foundPlayer.getOrElse(fail("either was not Right!"))
    foundResult.isDefined should be(true)
    foundResult.get.id shouldBe testPid
  }

  it should "throw exception on duplicate player id" in {
    val result: Result[Long] = createPlayer(testPid, testName)
    val expectedError = createPlayer(testPid, testName)
    expectedError.isLeft should be(true)
    inside(expectedError) {
      case Left(err) =>
        err.getMessage should include("Duplicate")
      case Right(_) => fail("DbException is expected")
    }
  }

  it should "throw exception on get user by id which not exist" in {
    val result: Result[Long] = createPlayer(testPid, testName)
    val foundPlayer = playerRepo.getById(1000).value.unsafeRunSync()
    foundPlayer.isRight should be(true)
    val foundResult = foundPlayer.getOrElse(fail("either was not Right!"))
    foundResult shouldBe None
  }

  it should "update existed player in db" in {
    val result: Result[Long] = createPlayer(testPid, testName)
    result.isRight should be(true)

    val updPlayer = Player(testPid, "Test4", None, admin = true, notificationsEnabled = false)
    val updatedPlayer = playerRepo.update(updPlayer).value.unsafeRunSync()
    updatedPlayer.isRight should be(true)

    val foundPlayer = playerRepo.getById(testPid).value.unsafeRunSync()
    foundPlayer.isRight should be(true)
    val foundResult = foundPlayer.getOrElse(fail("either was not Right!"))
    foundResult.isDefined should be(true)
    foundResult.get shouldBe updPlayer
  }

  it should "find users by surnames" in {
    createPlayer(testPid, testName)
    createPlayer(100, "Test2")
    createPlayer(101, "Test3")
    val result = playerRepo.findPlayers(List(testName, "Test2")).value.unsafeRunSync()
    result.isRight should be(true)
    result.getOrElse(fail("either was not Right!")).size shouldBe 2
  }

  it should "find user by id" in {
    createPlayer(testPid, testName)
    createPlayer(100, "Test2")
    val result = playerRepo.getById(100).value.unsafeRunSync()
    result.isRight should be(true)
    val maybePlayer = result.getOrElse(fail("either was not Right!"))
    maybePlayer.isDefined shouldBe true
    maybePlayer.get.surname shouldBe "Test2"
  }

  it should "clear table" in {
    createPlayer(testPid, testName)
    createPlayer(100, "Test2")
    createPlayer(101, "Test3")
    playerRepo.clearTable.value.unsafeRunSync()
    val result = playerRepo.findPlayers(List(testName)).value.unsafeRunSync()
    result.isRight should be(true)
    result.getOrElse(fail("either was not Right!")).size shouldBe 0
  }

  private def createPlayer(id: Long, name: String): Result[Long] = {
    val player = Player(id, name, Some("9z10y"), admin = false, notificationsEnabled = false)
    playerRepo.insert(player).value.unsafeRunSync()
  }

  override protected def afterEach(): Unit = {
    playerRepo.clearTable.value.unsafeRunSync()
  }

  override protected def beforeEach(): Unit = {
    playerRepo.clearTable.value.unsafeRunSync()
  }
}
