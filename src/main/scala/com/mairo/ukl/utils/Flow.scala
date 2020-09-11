package com.mairo.ukl.utils

import cats.data.EitherT
import cats.implicits._
import cats.{Applicative, Monad}

object Flow {
  type Result[T] = Either[Throwable, T]
  type Flow[F[_], T] = EitherT[F, Throwable, T]

  def toRightResult[F[_] : Applicative, R](data: R): F[Result[R]] = {
    Applicative[F].pure(data.asRight[Throwable])
  }

  def toLeftResult[F[_] : Applicative, R](data: Throwable): F[Result[R]] = {
    Applicative[F].pure(data.asLeft[R])
  }

  def fromResult[F[_] : Applicative, T](data: Result[T]): Flow[F, T] = {
    EitherT(Applicative[F].pure(data))
  }

  def pureRight[F[_] : Applicative, T](data: T): Flow[F, T] = {
    EitherT(Applicative[F].pure(data.asRight[Throwable]))
  }

  def pureLeft[F[_] : Applicative, R](data: Throwable): Flow[F, R] = {
    EitherT(Applicative[F].pure(data.asLeft[R]))
  }

  def liftResult[F[_] : Applicative, T](data: Result[T]): Flow[F, T] = {
    EitherT(Applicative[F].pure(data))
  }

  def fromFResult[F[_], T](f: F[Result[T]]): Flow[F, T] = {
    EitherT(f)
  }

  def fromF[F[_] : Monad, T](fa: F[T]): Flow[F, T] = {
    EitherT(Monad[F].map(fa)(x => x.asRight[Throwable]))
  }

  def log[F[_] : Monad](logF: F[Unit]): Flow[F, Unit] = {
    EitherT(Monad[F].map(logF)(x => x.asRight[Throwable]))
  }
}
