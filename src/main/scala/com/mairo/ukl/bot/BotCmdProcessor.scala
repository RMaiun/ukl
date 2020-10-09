package com.mairo.ukl.bot

import cats.Monad
import cats.effect.Sync
import com.mairo.ukl.dtos.BotResponse
import com.mairo.ukl.helper.ConfigProvider.Config
import com.mairo.ukl.helper.MessageFormatter
import com.mairo.ukl.rabbit.RabbitProducer
import com.mairo.ukl.services.PlayerService
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow

trait BotCmdProcessor[F[_]] {
  def listPlayersCmd(chatId: String): Flow[F, Unit]
}

object BotCmdProcessor {
  def apply[F[_], T](implicit ev: BotCmdProcessor[F]): BotCmdProcessor[F] = ev


  def impl[F[_] : Monad : Sync, FoundPlayers](PlayerService: PlayerService[F],
                                              RabbitProducer: RabbitProducer[F])(implicit config: Config): BotCmdProcessor[F] =
    new BotCmdProcessor[F] {
      override def listPlayersCmd(chatId: String): Flow[F, Unit] = {
        val result: Flow[F, Unit] = for {
          players <- PlayerService.findAllPlayers
          str <- MessageFormatter.formatPlayers(players)
          _ <- RabbitProducer.publish(BotResponse(chatId, str), config.rabbit.outputChannel)
        } yield ()
        withErrorCheck(chatId)(result)
      }


      private def withErrorCheck(chatId: String)(f: Flow[F, Unit]): Flow[F, Unit] = {
        f.leftFlatMap(err => logAndSend(err, BotResponse(chatId, err.getMessage)))
      }

      private def logAndSend(err: Throwable, botResponse: BotResponse): Flow[F, Unit] = {
        for {
          _ <- Flow.fromF(Sync[F].delay(err.printStackTrace()))
          _ <- RabbitProducer.publish(botResponse, config.rabbit.errorChannel)
        } yield ()
      }
    }

}
