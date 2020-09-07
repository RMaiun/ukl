package com.mairo.ukl.errors

object UklException {

  case class LoadConfigError(msg: String) extends RuntimeException(msg)

}