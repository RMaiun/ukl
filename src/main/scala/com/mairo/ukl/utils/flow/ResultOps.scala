package com.mairo.ukl.utils.flow

import cats.syntax.either._

object ResultOps {
  type Result[T] = Either[Throwable, T]

  def fromOption[T](maybeData: Option[T], ex: Throwable): Result[T] = {
    maybeData match {
      case Some(value) => value.asRight[Throwable]
      case None => ex.asLeft[T]
    }
  }

  def error[T](err: Throwable): Result[T] = {
    err.asLeft[T]
  }
}
