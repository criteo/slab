package com.criteo.slab.app

import java.net.URLDecoder

import com.criteo.slab.core.{Board, Context, NoopValueStore, ValueStore}
import com.criteo.slab.utils.Jsonable._
import lol.http.{Server, _}
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class WebServer(val boards: Seq[Board])(implicit valueStore: ValueStore = NoopValueStore, ec: ExecutionContext) {
  lazy val logger = LoggerFactory.getLogger(this.getClass)

  private val boardsMap: Map[String, Board] = boards.foldLeft(Map.empty[String, Board]) {
    case (acc, board) => acc + (board.title -> board)
  }

  def apply(port: Int): Unit = {
    logger.info(s"starting server at port: $port")
    Server.listen(port) {
      case GET at url"/api/boards/$board" => {
        logger.info(s"GET /api/boards/$board")
        val boardName = URLDecoder.decode(board, "UTF-8")
        boardsMap.get(boardName).fold(Future.successful(NotFound(s"Board $boardName does not exist"))) { board =>
          board.apply(None)
            .map(view => BoardResponse(view, board.layout, board.links).toJSON)
            .map(Ok(_).addHeaders(HttpString("content-type") -> HttpString("application/json")))
        }
      }
      case GET at url"/api/boards/$board/history/$timestamp" => {
        logger.info(s"GET /api/boards/$board/history/$timestamp")
        val boardName = URLDecoder.decode(board, "UTF-8")
        boardsMap.get(boardName).fold(Future.successful(NotFound(s"Board $boardName does not exist"))) { board =>
          Try(timestamp.toInt).map(new DateTime(_)).toOption.fold(
            Future.successful(BadRequest("invalid timestamp"))
          ) { dateTime =>
            board.apply(Some(Context(dateTime)))
              .map(view => BoardResponse(view, board.layout, board.links).toJSON)
              .map(Ok(_).addHeaders(HttpString("content-type") -> HttpString("application/json")))
          }
        }
      }
    }
    logger.info(s"Listening to $port")
  }
}
