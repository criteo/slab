package com.criteo.slab.app

import java.net.URLDecoder

import com.criteo.slab.core._
import com.criteo.slab.utils.Jsonable._
import lol.http._
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class WebServer(val boards: Seq[Board])(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val boardsMap: Map[String, Board] = boards.foldLeft(Map.empty[String, Board]) {
    case (acc, board) => acc + (board.title -> board)
  }

  private val stateService = new StateService(boards, 60)

  private val routes: PartialFunction[Request, Future[Response]] = {
    case GET at url"/api/boards" => {
      // Configs of boards
      logger.info(s"GET /api/boards")
      Ok(
        boards.map { board =>
          BoardConfig(board.title, board.layout, board.links)
        }.toList.toJSON
      ).addHeaders(HttpString("content-type") -> HttpString("application/json"))
    }
    case GET at url"/api/boards/$board" => {
      // Current board view
      logger.info(s"GET /api/boards/$board")
      val boardName = URLDecoder.decode(board, "UTF-8")
      boardsMap.get(boardName).fold(Future.successful(NotFound(s"Board $boardName does not exist"))) { board =>
        stateService
          .getCurrent(boardName)
          .fold(
            Future.successful(Response(412)) // Not yet ready
          ) { view: ReadableView =>
            Future.successful(
              Ok(view.toJSON).addHeaders(HttpString("content-type") -> HttpString("application/json")
              )
            )
          }
      }
    }
    case GET at url"/api/boards/$board/history/$timestamp" => {
      logger.info(s"GET /api/boards/$board/history/$timestamp")
      val boardName = URLDecoder.decode(board, "UTF-8")
      boardsMap.get(boardName).fold(Future.successful(NotFound(s"Board $boardName does not exist"))) { board =>
        Try(timestamp.toLong).map(new DateTime(_)).toOption.fold(
          Future.successful(BadRequest("invalid timestamp"))
        ) { dateTime =>
          board.apply(Some(Context(dateTime)))
            .map((_: ReadableView).toJSON)
            .map(Ok(_).addHeaders(HttpString("content-type") -> HttpString("application/json")))
        }
      }
    }
    case GET at url"/api/boards/$board/history?last" => {
      logger.info(s"GET /api/boards/$board/history?last")
      val boardName = URLDecoder.decode(board, "UTF-8")
      boardsMap.get(boardName).fold(Future.successful(NotFound(s"Board $boardName does not exist"))) { board =>
        stateService.getHistory(boardName).fold(
          Future.successful(Response(412)) // Not yet ready
        ) { history: Map[Long, ReadableView] =>
          Ok(history.toJSON).addHeaders(HttpString("content-type") -> HttpString("application/json"))
        }
      }
    }
    case GET at url"/api/boards/$board/history?from=$fromTS&until=$untilTS" => {
      logger.info(s"GET /api/boards/$board/history?from=$fromTS&until=$untilTS")
      val boardName = URLDecoder.decode(board, "UTF-8")
      boardsMap.get(boardName).fold(Future.successful(NotFound(s"Board $boardName does not exist"))) { board =>
        val range = for {
          from <- Try(fromTS.toLong).map(new DateTime(_)).toOption
          until <- Try(untilTS.toLong).map(new DateTime(_)).toOption
        } yield (from, until)
        range.fold(Future.successful(BadRequest("Invalid timestamp"))) { case (from, until) =>
          board.fetchHistory(from, until)
            .map((_: Map[Long, ReadableView]).toJSON)
            .map(Ok(_).addHeaders(
              HttpString("content-type") -> HttpString("application/json")
            ))
        }
      }
    }
    case GET at url"/api/boards/$board/stats" => {
      logger.info(s"GET /api/boards/$board/stats")
      val boardName = URLDecoder.decode(board, "UTF-8")
      boardsMap.get(boardName).fold(Future.successful(NotFound(s"Board $boardName does not exist"))) { board =>
        Future.successful {
          stateService.getStats(boardName)
            .map(_.toJSON)
            .map(Ok(_).addHeaders(
              HttpString("content-type") -> HttpString("application/json")
            ))
            .getOrElse(Response(412))
        }
      }
    }
    case GET at url"/$file[.]$ext" => {
      logger.info(s"GET /$file.$ext")
      ClasspathResource(s"/$file.$ext").fold(NotFound())(r => Ok(r))
    }
    case req if req.method == GET => {
      logger.info(s"GET ${req.url}")
      if (req.url.startsWith("/api"))
        NotFound
      else
        ClasspathResource("/index.html").fold(NotFound())(r => Ok(r))
    }
  }

  private val notFound: PartialFunction[Request, Future[Response]] = {
    case anyReq => {
      logger.info(s"${anyReq.method.toString} ${anyReq.url} not found")
      Future.successful(Response(404))
    }
  }

  def apply(port: Int, customRoutes: PartialFunction[Request, Future[Response]] = PartialFunction.empty): Unit = {
    logger.info(s"Starting server at port: $port")
    stateService.start()

    Server.listen(port)(routes orElse customRoutes orElse notFound)
    logger.info(s"Listening to $port")

    sys.addShutdownHook {
      logger.info("Shutting down WebServer")
    }
  }
}
