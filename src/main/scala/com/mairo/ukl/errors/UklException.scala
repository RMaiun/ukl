package com.mairo.ukl.errors

object UklException {

  case class DbException(cause:Throwable) extends RuntimeException(cause)
}