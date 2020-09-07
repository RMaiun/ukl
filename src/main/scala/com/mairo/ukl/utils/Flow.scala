package com.mairo.ukl.utils

import cats.Applicative
import cats.data.EitherT
import cats.implicits._

object Flow {
  type Result[T] = Either[Throwable, T]
  type Flow[F[_], T] = F[Result[T]]
  type FlowT[F[_], T] = EitherT[F, Throwable, T]

  def right[F[_] : Applicative, T](data: T): Flow[F, T] = {
    Applicative[F].pure(data.asRight[Throwable])
  }

  def left[F[_] : Applicative, R](data: Throwable): Flow[F, R] = {
    Applicative[F].pure(data.asLeft[R])
  }

  def fromResult[F[_] : Applicative, T](data: Result[T]): FlowT[F, T] = {
    EitherT(Applicative[F].pure(data))
  }

  def rightT[F[_] : Applicative, T](data: T): FlowT[F, T] = {
    EitherT(Applicative[F].pure(data.asRight[Throwable]))
  }

  def leftT[F[_] : Applicative, R](data: Throwable): FlowT[F, R] = {
    EitherT(Applicative[F].pure(data.asLeft[R]))
  }

  def fromResultT[F[_] : Applicative, T](data: Result[T]): FlowT[F, T] = {
    EitherT(Applicative[F].pure(data))
  }
}
