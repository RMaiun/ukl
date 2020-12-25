package com.mairo.ukl.postprocessor

import com.mairo.ukl.processor.CommandObjects.BotInputMessage
import com.mairo.ukl.utils.Flow.Flow
import com.mairo.ukl.utils.{Commands, ParseSupport}

trait CommandPostProcessor[F[_]] extends ParseSupport[F] with Commands{
  def postProcess(input:BotInputMessage):Flow[F,Unit]
}
