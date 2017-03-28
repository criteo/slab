package com.criteo.slab.app

import java.net.URLDecoder

import com.criteo.slab.core.{Board, Context, NoopValueStore, ValueStore}
import com.criteo.slab.utils.Jsonable._
import lol.http._
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class WebServer(val boards: Seq[Board])(implicit ec: ExecutionContext) {
  lazy val logger = LoggerFactory.getLogger(this.getClass)

  private val boardsMap: Map[String, Board] = boards.foldLeft(Map.empty[String, Board]) {
    case (acc, board) => acc + (board.title -> board)
  }

  def apply(port: Int): Unit = {
    logger.info(s"starting server at port: $port")
    Server.listen(port) {
      case GET at url"/api/boards" => {
        logger.info(s"GET /api/boards")
        Ok(boardsMap.keys.toJSON).addHeaders(HttpString("content-type") -> HttpString("application/json"))
      }
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
          Try(timestamp.toLong).map(new DateTime(_)).toOption.fold(
            Future.successful(BadRequest("invalid timestamp"))
          ) { dateTime =>
            board.apply(Some(Context(dateTime)))
              .map(view => BoardResponse(view, board.layout, board.links).toJSON)
              .map(Ok(_).addHeaders(HttpString("content-type") -> HttpString("application/json")))
          }
        }
      }
      case GET at url"/api/boards/$board/$box/timeseries?from=$from&until=$until" => {
        logger.info(s"GET /api/boards/$board/$box/timeseries")
        val boardName = URLDecoder.decode(board, "UTF-8")
        boardsMap.get(boardName).fold(Future.successful(NotFound(s"Board $boardName does not exist"))) { board =>
          val boxName = URLDecoder.decode(box, "UTF-8")
          val range = for {
            f <- Try(from.toLong).map(new DateTime(_)).toOption
            u <- Try(until.toLong).map(new DateTime(_)).toOption
          } yield (f, u)
          range.fold(Future.successful(BadRequest("Invalid timestamp"))) { case (from, until) =>
            board.fetchTimeSeries(boxName, from, until).map {
              case Some(ts) => Ok(ts.toJSON).addHeaders(HttpString("content-type") -> HttpString("application/json"))
              case None => NotFound(s"Box $boxName does not exist in $board")
            }
          }
        }
      }
    }
    logger.info(s"Listening to $port")
  }
}
