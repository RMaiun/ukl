package com.mairo.ukl.processor

import io.circe.Json

object CommandObjects {

  case class BotInputMessage(cmd: String, chatId: String, data: Option[Json])

  case class BotOutputMessage(chatId: String, result: String, msgId: Int = -1)

}
