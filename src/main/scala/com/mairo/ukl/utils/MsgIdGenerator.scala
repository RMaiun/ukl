package com.mairo.ukl.utils

trait MsgIdGenerator {
  def msgId(): Int = (System.currentTimeMillis() % Int.MaxValue).toInt
}
