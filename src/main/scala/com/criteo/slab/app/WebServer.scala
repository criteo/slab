package com.criteo.slab.app

import java.net.URLDecoder
import java.time.{Duration, Instant}

import com.criteo.slab.app.StateService.NotFoundError
import com.criteo.slab.core._
import com.criteo.slab.utils.Jsonable
import com.criteo.slab.utils.Jsonable._
import lol.http._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

/** Slab Web server
  *
  * @param boards          The list of boards
  * @param pollingInterval The polling interval in seconds
  * @param statsDays       Specifies how many days of history to be counted into statistics
  * @param customRoutes    Defines custom routes (should starts with "/api")
  * @param ec              The execution context for the web server
  */
class WebServer(
                 val boards: Seq[Board],
                 val pollingInterval: Int = 60,
                 val statsDays: Int = 7,
                 val customRoutes: PartialFunction[Request, Future[Response]] = PartialFunction.empty)(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val boardsMap: Map[String, Board] = boards.foldLeft(Map.empty[String, Board]) {
    case (acc, board) => acc + (board.title -> board)
  }

  private val stateService = new StateService(boards, pollingInterval, statsDays)

  private implicit def stringDecoder = new Jsonable[String] {}

  private val routes: PartialFunction[Request, Future[Response]] = {
    // Configs of boards
    case GET at url"/api/boards" => {
      Ok(boards.map { board => BoardConfig(board.title, board.layout, board.links) }.toList.toJSON).map(jsonContentType)
    }
    // Current board view
    case GET at url"/api/boards/$board" => {
      val boardName = URLDecoder.decode(board, "UTF-8")
      stateService
        .current(boardName)
        .map((_: ReadableView).toJSON)
        .map(Ok(_))
        .map(jsonContentType)
        .recoverWith(errorHandler)
    }
    // Snapshot of the given time point
    case GET at url"/api/boards/$board/snapshot/$timestamp" => {
      val boardName = URLDecoder.decode(board, "UTF-8")
      boardsMap.get(boardName).fold(Future.successful(NotFound(s"Board $boardName does not exist"))) { board =>
        Try(timestamp.toLong).map(Instant.ofEpochMilli).toOption.fold(
          Future.successful(BadRequest("invalid timestamp"))
        ) { dateTime =>
          board.apply(Some(Context(dateTime)))
            .map((_: ReadableView).toJSON)
            .map(Ok(_))
            .map(jsonContentType)
        }
      }.recoverWith(errorHandler)
    }
    // History of last 24 hours
    case GET at url"/api/boards/$board/history?last" => {
      val boardName = URLDecoder.decode(board, "UTF-8")
      stateService
        .history(boardName)
        .map { h: Map[Long, String] => Ok(h.toJSON) }
        .map(jsonContentType)
        .recoverWith(errorHandler)
    }
    // History of the given range
    case GET at url"/api/boards/$board/history?from=$fromTS&until=$untilTS" => {
      val boardName = URLDecoder.decode(board, "UTF-8")
      boardsMap.get(boardName).fold(Future.successful(NotFound(s"Board $boardName does not exist"))) { board =>
        val range = for {
          from <- Try(fromTS.toLong).map(Instant.ofEpochMilli).toOption
          until <- Try(untilTS.toLong).map(Instant.ofEpochMilli).toOption
        } yield (from, until)
        range.fold(Future.successful(BadRequest("Invalid timestamp"))) { case (from, until) =>
          board.fetchHistory(from, until)
            .map(_.mapValues(_.status.name).toJSON)
            .map(Ok(_))
            .map(jsonContentType)
        }
      }.recoverWith(errorHandler)
    }
    // Stats of the board
    case GET at url"/api/boards/$board/stats" => {
      val boardName = URLDecoder.decode(board, "UTF-8")
      stateService.stats(boardName)
        .map(_.toJSON)
        .map(Ok(_))
        .map(jsonContentType)
        .recoverWith(errorHandler)
    }
    // Static resources
    case GET at url"/$file.$ext" => {
      ClasspathResource(s"/$file.$ext").fold(NotFound())(r => Ok(r))
    }
    case req if req.method == GET && !req.url.startsWith("/api") => {
      ClasspathResource("/index.html").fold(NotFound())(r => Ok(r))
    }
  }

  private def notFound: PartialFunction[Request, Future[Response]] = {
    case anyReq => {
      logger.info(s"${anyReq.method.toString} ${anyReq.url} not found")
      Response(404)
    }
  }

  private def errorHandler: PartialFunction[Throwable, Future[Response]] = {
    case f: NotFoundError =>
      NotFound(f.message)
    case NonFatal(e) =>
      logger.error(e.getMessage, e)
      InternalServerError()
  }

  private def jsonContentType(res: Response) = res.addHeaders(HttpString("content-type") -> HttpString("application/json"))

  private def routeLogger(router: Request => Future[Response]) = (request: Request) => {
    val start = Instant.now()
    router(request) map { res =>
      val duration = Duration.between(start, Instant.now)
      logger.info(s"${request.method} ${request.url} - ${res.status} ${duration.toMillis}ms")
      res
    }
  }

  /**
    * Start the web server
    *
    * @param port The server's port
    */
  def apply(port: Int): Unit = {
    logger.info(s"Starting server at port: $port")
    stateService.start()

    Server.listen(port)(routeLogger(routes orElse customRoutes orElse notFound))
    logger.info(s"Listening to $port")

    sys.addShutdownHook {
      logger.info("Shutting down WebServer")
    }
  }
}
