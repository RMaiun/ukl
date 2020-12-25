package com.mairo.ukl.services.impl

import cats.Monad
import cats.effect.{Sync, Timer}
import com.mairo.ukl.errors.UklException.{InvalidBotRequest, InvalidCommandException}
import com.mairo.ukl.postprocessor.CommandPostProcessor
import com.mairo.ukl.processor.CommandObjects.{BotInputMessage, BotOutputMessage}
import com.mairo.ukl.processor.CommandProcessor
import com.mairo.ukl.rabbit.{RabbitSender, TelegramBotCommandHandler}
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow
import io.circe._
import io.circe.optics.JsonPath._
import io.circe.parser._

class TelegramBotCommandHandlerImpl[F[_] : Monad : Sync : Timer](processors: Seq[CommandProcessor[F]],
                                                                 postProcessors: Seq[CommandPostProcessor[F]],
                                                                 rabbitSender: RabbitSender[F])
  extends TelegramBotCommandHandler[F] {

  override def handleCmd(msg: String): Flow[F, Unit] = {
    val json: Json = parse(msg).getOrElse(Json.Null)
    val cmdPath = root.cmd.string
    val chatIdPath = root.chatId.string
    val dataPath = root.data.json
    val cmdVal = cmdPath.getOption(json)
    val chatIdOpt = chatIdPath.getOption(json)
    val dataOpt = dataPath.getOption(json)

    val maybeResult = for {
      cmd <- cmdVal
      chatId <- chatIdOpt
    } yield {
      processCommand(BotInputMessage(cmd, chatId, dataOpt))
    }

    maybeResult match {
      case Some(value) => value
      case None =>
        Flow.fromF(Sync[F].delay(InvalidBotRequest(chatIdOpt, cmdVal, dataOpt).printStackTrace()))
    }
  }

  private def processCommand(input: BotInputMessage): Flow[F, Unit] = {
    val processorResult = invokeProcessor(input)
    processorResult.biflatMap(
      err => sendErrorResponse(input.chatId, err),
      data => sendWithPostProcessing(input, data)
    )
  }

  private def invokeProcessor(input: BotInputMessage): Flow[F, BotOutputMessage] = {
    for {
      processor <- selectProcessor(input.cmd)
      output <- processor.process(input)
    } yield output
  }

  private def sendWithPostProcessing(input: BotInputMessage, output: BotOutputMessage): Flow[F, Unit] = {
    for {
      _ <- rabbitSender.publish(output)
      postProcessor <- selectPostProcessor(input.cmd)
      _ <- postProcessor.postProcess(input)
    } yield ()
  }

  private def sendErrorResponse(chatId: String, err: Throwable): Flow[F, Unit] = {
    rabbitSender.publish(BotOutputMessage(chatId, err.getMessage, msgId()))
  }

  private def selectProcessor(cmd: String): Flow[F, CommandProcessor[F]] = {
    Flow.fromOption(processors.find(p => p.commands().contains(cmd)), InvalidCommandException(cmd))
  }

  private def selectPostProcessor(cmd: String): Flow[F, CommandPostProcessor[F]] = {
    Flow.fromOption(postProcessors.find(p => p.commands().contains(cmd)), InvalidCommandException(cmd))
  }

  private def msgId(): Int = (System.currentTimeMillis() % Int.MaxValue).toInt
}
