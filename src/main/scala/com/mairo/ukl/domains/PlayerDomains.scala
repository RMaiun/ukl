package com.mairo.ukl.domains

object PlayerDomains {

  case class Player(id: Long,
                    surname: String,
                    tid: Option[String],
                    cid: Option[String],
                    admin: Boolean)

}
