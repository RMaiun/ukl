package com.mairo.ukl.bot

import cats.Monad
import cats.effect.Sync
import com.mairo.ukl.domains.Player
import com.mairo.ukl.dtos.{AddRoundDto, BotResponse}
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.helper.MessageFormatter
import com.mairo.ukl.rabbit.RabbitProducer
import com.mairo.ukl.services.{PlayerService, RoundService}
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow

trait BotCmdProcessor[F[_]] {
  def listPlayersCmd(chatId: String, msgId: Int): Flow[F, Unit]

  def addRoundCmd(chatId: String, msgId: Int, dto: AddRoundDto): Flow[F, Unit]
}

object BotCmdProcessor {
  val rng = new scala.util.Random(0L)


  def apply[F[_], T](implicit ev: BotCmdProcessor[F]): BotCmdProcessor[F] = ev


  def impl[F[_] : Monad : Sync, FoundPlayers](PlayerService: PlayerService[F],
                                              RoundService: RoundService[F],
                                              RabbitProducer: RabbitProducer[F])(implicit config: Config): BotCmdProcessor[F] =
    new BotCmdProcessor[F] {
      override def listPlayersCmd(chatId: String, msgId: Int): Flow[F, Unit] = {
        val result: Flow[F, Unit] = for {
          players <- PlayerService.findAllPlayers
          str <- MessageFormatter.formatPlayers(players)
          _ <- RabbitProducer.publish(BotResponse(msgId, chatId, str), config.rabbit.outputChannel)
        } yield ()
        withErrorCheck(chatId, msgId)(result)
      }

      override def addRoundCmd(chatId: String, msgId: Int, dto: AddRoundDto): Flow[F, Unit] = {
        for {
          id <- RoundService.saveRound(dto)
          str <- MessageFormatter.formatStoredId(id)
          w1 <- PlayerService.findPlayer(dto.w1)
          w2 <- PlayerService.findPlayer(dto.w2)
          l1 <- PlayerService.findPlayer(dto.l1)
          l2 <- PlayerService.findPlayer(dto.l2)
          _ <- RabbitProducer.publish(BotResponse(msgId, chatId, str), config.rabbit.outputChannel)
          _ <- sendNotification(w1, dto.l1.capitalize, dto.l2.capitalize, isWinner = true)
          _ <- sendNotification(w2, dto.l1.capitalize, dto.l2.capitalize, isWinner = true)
          _ <- sendNotification(l1, dto.w1.capitalize, dto.w2.capitalize, isWinner = false)
          _ <- sendNotification(l2, dto.w1.capitalize, dto.w2.capitalize, isWinner = false)
        } yield ()
      }

      private def sendNotification(player: Player, op1: String, op2: String, isWinner: Boolean): Flow[F, Unit] = {
        player.tid match {
          case Some(chatId) =>
            for {
              msg <- MessageFormatter.formatPlayerNotification(op1, op2, isWinner)
              _ <- RabbitProducer.publish(BotResponse(rng.nextInt(), chatId, msg), config.rabbit.outputChannel)
            } yield ()
          case None => Flow.unit
        }


      }

      private def withErrorCheck(chatId: String, msgId: Int)(f: Flow[F, Unit]): Flow[F, Unit] = {
        f.leftFlatMap(err => logAndSend(err, BotResponse(msgId, chatId, err.getMessage)))
      }

      private def logAndSend(err: Throwable, botResponse: BotResponse): Flow[F, Unit] = {
        for {
          _ <- Flow.fromF(Sync[F].delay(err.printStackTrace()))
          _ <- RabbitProducer.publish(botResponse, config.rabbit.errorChannel)
        } yield ()
      }
    }

}
