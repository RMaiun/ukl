package com.mairo.ukl.zio.services

import com.mairo.ukl.zio.repositories.SeasonRepository.HasSeasonRepo
import io.circe.{ Decoder, Encoder }
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import zio._
import zio.interop.catz._
import cats.effect._
import com.mairo.ukl.zio.repositories.SeasonRepository
import com.mairo.ukl.zio.services.codecs._
object SeasonService {

  def routes[R <: HasSeasonRepo](): HttpRoutes[RIO[R, *]] = {
    type TodoTask[A] = RIO[R, A]

    val dsl: Http4sDsl[TodoTask] = Http4sDsl[TodoTask]
    import dsl._

    implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[TodoTask, A] = jsonOf[TodoTask, A]
    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[TodoTask, A] = jsonEncoderOf[TodoTask, A]

    HttpRoutes.of[TodoTask] {
      case GET -> Root / "get" / id =>
        for {
          s        <- SeasonRepository.getSeason(id)
          response <- s.fold(NotFound())(x => Ok(SeasonDto(x.name)))
        } yield response

      case GET -> Root / "all" =>
        Ok(SeasonRepository.listAll.map(_.map(s => SeasonDto(s.name))))

//      case req @ POST -> Root =>
//        req.decode[TodoItemPostForm] { todoItemForm =>
//          TodoRepository
//            .create(todoItemForm)
//            .map(TodoItemWithUri(rootUri, _))
//            .flatMap(Created(_))
//        }
//
//      case DELETE -> Root / LongVar(id) =>
//        for {
//          item   <- TodoRepository.getById(TodoId(id))
//          result <- item
//                      .map(x => TodoRepository.delete(x.id))
//                      .fold(NotFound())(_.flatMap(Ok(_)))
//        } yield result
//
//      case DELETE -> Root =>
//        TodoRepository.deleteAll *> Ok()
//
//      case req @ PATCH -> Root / LongVar(id) =>
//        req.decode[TodoItemPatchForm] { updateForm =>
//          for {
//            update   <- TodoRepository.update(TodoId(id), updateForm)
//            response <- update.fold(NotFound())(x => Ok(TodoItemWithUri(rootUri, x)))
//          } yield response
//        }
    }
  }
}
