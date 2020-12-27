package com.mairo.ukl.utils.flow

import cats.Functor
import cats.data.EitherT
import cats.syntax.either._
import com.mairo.ukl.utils.flow.Flow.Flow
import io.chrisdavenport.log4cats.Logger

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
