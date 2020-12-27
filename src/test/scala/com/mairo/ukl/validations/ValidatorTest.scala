package com.mairo.ukl.validations

import cats.effect.IO
import com.mairo.ukl.dtos.AddPlayerDto
import org.scalatest.Inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ValidationSet.AddPlayerDtoValidator
class ValidatorTest extends AnyFlatSpec with Matchers with Inside {

  "Validator" should "return validation error" in {
    val addPlayerDto = AddPlayerDto("x", None, admin = false, "x2")
    val result: Either[Throwable, Unit] = Validator.validateDto[IO, AddPlayerDto](addPlayerDto).value
      .unsafeRunSync()
    result.isLeft should be(true)
    inside(result) {
      case Left(err) =>
        err.getMessage.split(";").length should be(2)
      case Right(_) => fail("Validation exception is expected")
    }
  }
}
