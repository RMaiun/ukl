package com.mairo.ukl.utils

import cats.data.EitherT
import cats.implicits._
import cats.{Applicative, Functor, Monad}
import io.chrisdavenport.log4cats.Logger

object Flow {
  type Result[T] = Either[Throwable, T]
  type Flow[F[_], T] = EitherT[F, Throwable, T]

  object FlowLog {
    def info[F[_] : Logger : Functor](msg: String): Flow[F, Unit] = {
      EitherT(Functor[F].map(Logger[F].info(msg))(_.asRight[Throwable]))
    }

    def warn[F[_] : Logger : Functor](msg: String): Flow[F, Unit] = {
      EitherT(Functor[F].map(Logger[F].warn(msg))(_.asRight[Throwable]))
    }

    def error[F[_] : Logger : Functor](msg: String): Flow[F, Unit] = {
      EitherT(Functor[F].map(Logger[F].error(msg))(_.asRight[Throwable]))
    }
  }

  def apply[F[_], T](f: F[Result[T]]): Flow[F, T] = {
    EitherT(f)
  }

  def toRightResult[F[_] : Applicative, R](data: R): F[Result[R]] = {
    Applicative[F].pure(data.asRight[Throwable])
  }

  def toLeftResult[F[_] : Applicative, R](data: Throwable): F[Result[R]] = {
    Applicative[F].pure(data.asLeft[R])
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

  def fromF[F[_] : Monad, T](fa: F[T]): Flow[F, T] = {
    EitherT(Monad[F].map(fa)(x => x.asRight[Throwable]))
  }
}
