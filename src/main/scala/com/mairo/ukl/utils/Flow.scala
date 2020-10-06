package com.mairo.ukl.utils

import cats.data.EitherT
import cats.{Applicative, Monad}
import com.mairo.ukl.utils.ResultOps.Result
import cats.syntax.either._
object Flow {
  type Flow[F[_], T] = EitherT[F, Throwable, T]

  def apply[F[_], T](f: F[Result[T]]): Flow[F, T] = {
    EitherT(f)
  }

  def toRightResult[F[_] : Applicative, R](data: R): F[Result[R]] = {
    Applicative[F].pure(data.asRight[Throwable])
  }

  def toLeftResult[F[_] : Applicative, R](data: Throwable): F[Result[R]] = {
    Applicative[F].pure(data.asLeft[R])
  }

  def fromOption[F[_] : Applicative, T](data: Option[T], ex: Throwable): Flow[F, T] = {
    EitherT.fromOption[F](data, ex)
  }

  def fromRes[F[_] : Applicative, T](data: Result[T]): Flow[F, T] = {
    EitherT.fromEither[F](data)
  }

  def pure[F[_] : Applicative, T](data: T): Flow[F, T] = {
    EitherT(Applicative[F].pure(data.asRight[Throwable]))
  }

  def error[F[_] : Applicative, R](data: Throwable): Flow[F, R] = {
    EitherT(Applicative[F].pure(data.asLeft[R]))
  }

  def unit[F[_] : Applicative, R]: Flow[F, Unit] = {
    val unit: Either[Throwable, Unit] = Right(())
    EitherT(Applicative[F].pure(unit))

  }

  def fromF[F[_] : Monad, T](fa: F[T]): Flow[F, T] = {
    EitherT(Monad[F].map(fa)(x => x.asRight[Throwable]))
  }
}
