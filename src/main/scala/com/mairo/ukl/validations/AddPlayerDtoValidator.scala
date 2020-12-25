package com.mairo.ukl.validations

import cats.Monad
import com.mairo.ukl.dtos.AddPlayerDto
import com.mairo.ukl.utils.Flow.Flow
import cats.syntax.validated._
import cats.implicits._
import com.mairo.ukl.utils.ResultOps.Result
object AddPlayerDtoValidator extends ValidationRules {
  def validate[F[_]:Monad](dto:AddPlayerDto):Result[AddPlayerDto] = {
    (
      onlyNumbers("moderator")(dto.moderator),
      notEmpty("moderator")(dto.moderator),
      onlyLetters("surname")(dto.surname),
      length("surname")(2,20)(dto.surname)

    ).mapN()
  }
}
