package com.mairo.ukl.zio

import java.time.ZonedDateTime

import reactivemongo.api.bson.BSONObjectID

package object repositories {

  case class Season(_id: BSONObjectID, name: String, seasonEndNotification: Option[ZonedDateTime])
}
