package com.mairo.ukl.helper

import java.time.format.{DateTimeFormatter, TextStyle}
import java.time.{LocalDateTime, ZoneId}
import java.util.Locale

object DateFormatter {
  val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
  val EET_ZONE: ZoneId = ZoneId.of("Europe/Kiev")

  def now(): LocalDateTime = {
    LocalDateTime.now()
  }

  def formatDateWithHour(date: LocalDateTime): String = {
    val month = date.getMonth.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    val day = date.getDayOfMonth
    val dateTime = date.format(FORMATTER)
    val year = date.getYear
    s"$dateTime, $day, $month, $year"
  }
}
