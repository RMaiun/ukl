package com.mairo.ukl.services

import cats.effect.IO
import com.mairo.ukl.TestData
import com.mairo.ukl.domains.Player
import com.mairo.ukl.errors.UklException.InvalidUserRightsException
import com.mairo.ukl.repositories.PlayerRepository
import com.mairo.ukl.utils.flow.Flow
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterEach, Inside}

class UserRightsServiceTest extends AnyFlatSpec
  with Matchers
  with BeforeAndAfterEach
  with Inside
  with MockFactory {

  val playerRepo: PlayerRepository[IO] = mock[PlayerRepository[IO]]
  val userRightsService: UserRightsService[IO] = UserRightsService.impl[IO](playerRepo)

  "UserRightsService" should "define user as admin" in {
    (() => playerRepo.listAll).expects().returning(Flow.pure(TestData.players(true)))
    val result: Either[Throwable, Player] = userRightsService.checkUserIsAdmin("0003")
      .value
      .unsafeRunSync()
    result.isRight should be(true)
    result.getOrElse(fail("either was not Right!")).id shouldBe 3
  }

  it should "throw exception on duplicate player id" in {
    (() => playerRepo.listAll).expects().returning(Flow.pure(TestData.players(false)))
    val expectedError: Either[Throwable, Player] = userRightsService.checkUserIsAdmin("0003")
      .value
      .unsafeRunSync()
    expectedError.isLeft should be(true)
    inside(expectedError) {
      case Left(err) =>
        err.getMessage should be(InvalidUserRightsException().getMessage)
      case Right(_) => fail("Unexpected error is received")
    }
  }

}
