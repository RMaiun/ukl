package com.mairo.ukl

import java.time.LocalDateTime

import com.mairo.ukl.domains.{Player, Round, Season}

object TestData {

  def season(name: String): Season = {
    Season(1, name)
  }

  def players(admin: Boolean): List[Player] = {
    List(Player(1, "Test1", Some("0001"), None, admin),
      Player(2, "Test2", Some("0002"), None, admin),
      Player(3, "Test3", Some("0003"), None, admin),
      Player(4, "Test4", Some("0004"), None, admin),
    )
  }

  def rounds(num: Int): List[Round] = {
    List(
      Round(1, 1, 2, 3, 4, shutout = false, 1, LocalDateTime.now()),
      Round(2, 2, 3, 4, 1, shutout = true, 1, LocalDateTime.now()),
      Round(3, 3, 4, 1, 2, shutout = false, 1, LocalDateTime.now())
    )
  }
}
