package com.mairo.ukl.errors

import com.mairo.ukl.domains.PlayerDomains.Player

object UklException {

  case class DbException(cause:Throwable) extends RuntimeException(cause)
}