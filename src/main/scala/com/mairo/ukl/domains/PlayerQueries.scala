package com.mairo.ukl.domains

import cats.data.NonEmptyList
import com.mairo.ukl.domains.PlayerDomains.Player
import doobie._
import doobie.implicits._

object PlayerQueries {
  val invalidPlayer = "n/a"

  def findAllPlayers: doobie.Query0[Player] = {
    sql"SELECT * FROM player"
      .query[Player]
  }

  def deletePlayerById(id: Long): doobie.Update0 = {
    sql"DELETE FROM player where player.id = $id"
      .update
  }

  def clearTable: doobie.Update0 = {
    sql"DELETE FROM player"
      .update
  }

  def findPlayers(surnames: NonEmptyList[String]): doobie.Query0[Player] = {
    val fragment = fr" SELECT * FROM player WHERE " ++ Fragments.in(fr"player.surname", surnames)
    fragment.query[Player]
  }

  def getPlayerByName(name: String): doobie.Query0[Player] = {
    sql"SELECT * FROM player WHERE player.surname = $name LIMIT 1"
      .query[Player]
  }

  def getPlayerById(id: Long): doobie.Query0[Player] = {
    sql"SELECT * FROM player WHERE player.id = $id LIMIT 1"
      .query[Player]
  }

  def insertPlayer(id: Long, surname: String, tid: Option[String], cid: Option[String], admin: Boolean): doobie.Update0 = {
    sql"INSERT into player (id, surname, tid, cid, admin) VALUES ($id, $surname, $tid, $cid, $admin)"
      .update
  }

  def updatePlayer(player: Player): doobie.Update0 = {
    sql"""
         |UPDATE player
         |SET surname=${player.surname},
         | tid = ${player.tid},
         | cid = ${player.cid},
         | admin = ${player.admin}
         | WHERE id = ${player.id}
    """.stripMargin
      .update
  }

  def getLastPlayerId: doobie.Query0[Long] = {
    sql"SELECT player.id FROM player ORDER BY player.id DESC LIMIT 1"
      .query[Long]
  }

}
