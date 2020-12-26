package com.mairo.ukl.validations

import cats.Monad
import com.mairo.ukl.errors.UklException.ValidationException
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow
import com.wix.accord._


object Validator {
  def validateDto[F[_], T](data: T)(implicit v: Validator[T], F: Monad[F]): Flow[F, Unit] = {
    val vr = validate(data)(v)
    vr match {
      case Success => Flow.unit
      case Failure(violations) =>
        val errorMessages = violations.map(v => {
          val field = v.path.head match {
            case description: Descriptions.AssociativeDescription => description
            case Descriptions.Explicit(description) => description
            case Descriptions.Generic(description) => description
          }
          s"'$field' ${v.constraint} but was ${v.value}"
        })
        Flow.error(ValidationException(errorMessages))
    }
  }
}
