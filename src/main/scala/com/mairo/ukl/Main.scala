package com.mairo.ukl

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    UklServer.stream[IO].compile.drain.as(ExitCode.Success)
}