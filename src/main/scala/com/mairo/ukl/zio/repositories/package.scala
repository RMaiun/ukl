package com.mairo.ukl.zio

import java.time.ZonedDateTime

import reactivemongo.api.bson.BSONObjectID

package object repositories {

  case class Season(_id: BSONObjectID, name: String, seasonEndNotification: Option[ZonedDateTime])
  case class Player(
    _id: BSONObjectID,
    surname: String,
    tid: String,
    admin: Boolean = false,
    notificationsEnabled: Boolean = false
  )
}
