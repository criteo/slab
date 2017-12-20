package com.criteo.slab.app

import java.net.URLDecoder
import java.text.DecimalFormat
import java.time.{Duration, Instant}

import com.criteo.slab.app.StateService.NotFoundError
import com.criteo.slab.core.Executor.{FetchBoardHistory, FetchBoardHourlySlo, RunBoard}
import com.criteo.slab.core._
import com.criteo.slab.utils.Jsonable
import com.criteo.slab.utils.Jsonable._
import lol.http.{Request, Response, _}
import org.json4s.Serializer
import org.slf4j.LoggerFactory
import shapeless.HList
import shapeless.poly.Case3

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

/** Slab Web server
  *
  * @param pollingInterval The polling interval in seconds
  * @param statsDays       Specifies how many last days of statistics to be retained
  * @param routeGenerator  A function that generates custom routes (should starts with "/api")
  * @param executors       The executors of the boards
  * @param ec              The execution context for the web server
  */
case class WebServer(
                      val pollingInterval: Int = 60,
                      val statsDays: Int = 730,
                      private val routeGenerator: StateService => PartialFunction[Request, Future[Response]] = _ => PartialFunction.empty,
                      private val executors: List[Executor[_]] = List.empty
                    )(implicit ec: ExecutionContext) {
  /**
    * Attach a board to the server
    *
    * @param board The board
    * @return
    */
  def attach[L <: HList, O](board: Board[L])(
    implicit
    runBoard: Case3.Aux[RunBoard.type, Board[L], Context, Boolean, Future[BoardView]],
    fetchBoardHistory: Case3.Aux[FetchBoardHistory.type, Board[L], Instant, Instant, Future[Seq[(Long, BoardView)]]],
    fetchBoardHourlySlo: Case3.Aux[FetchBoardHourlySlo.type, Board[L], Instant, Instant, Future[Seq[(Long, Double)]]],
    store: Store[O]
  ): WebServer = {
    this.copy(executors = Executor(board) :: executors)
  }

  /**
    * Start the web server
    *
    * @param port The server's port
    */
  def apply(port: Int): Unit = {
    logger.info(s"Starting server at port: $port")
    stateService.start()

    Server.listen(port)(routeLogger(routes orElse routeGenerator(stateService) orElse notFound))
    logger.info(s"Listening to $port")

    sys.addShutdownHook {
      logger.info("Shutting down WebServer")
    }
  }

  /**
    *
    * @param generator A function that takes StateService and returns routes
    * @return Web server with the created routes
    */
  def withRoutes(generator: StateService => PartialFunction[Request, Future[Response]]) = this.copy(routeGenerator = generator)

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val decimalFormat = new DecimalFormat("0.####")

  private implicit def stringEncoder = new Jsonable[String] {}

  private implicit def longStringEncoder = new Jsonable[(Long, String)] {}

  private implicit def longStatsEncoder = new Jsonable[(Long, Stats)] {
    override val serializers: Seq[Serializer[_]] = implicitly[Jsonable[Stats]].serializers
  }

  private lazy val stateService = new StateService(executors, pollingInterval, statsDays)

  private lazy val boards = executors.map(_.board)

  private val routes: PartialFunction[Request, Future[Response]] = {
    // Configs of boards
    case GET at url"/api/boards" => {
      Ok(boards.map { board => BoardConfig(board.title, board.layout, board.links, board.slo) }.toJSON).map(jsonContentType)
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
      executors.find(_.board.title == boardName).fold(Future.successful(NotFound(s"Board $boardName does not exist"))) { executor =>
        Try(timestamp.toLong).map(Instant.ofEpochMilli).toOption.fold(
          Future.successful(BadRequest("invalid timestamp"))
        ) { dateTime =>
          executor.apply(Some(Context(dateTime)))
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
        .map(h => Ok(h.toJSON))
        .map(jsonContentType)
        .recoverWith(errorHandler)
    }
    // History of the given range
    case GET at url"/api/boards/$board/history?from=$fromTS&until=$untilTS" => {
      val boardName = URLDecoder.decode(board, "UTF-8")
      executors.find(_.board.title == boardName).fold(Future.successful(NotFound(s"Board $boardName does not exist"))) { executor =>
        val range = for {
          from <- Try(fromTS.toLong).map(Instant.ofEpochMilli).toOption
          until <- Try(untilTS.toLong).map(Instant.ofEpochMilli).toOption
        } yield (from, until)
        range.fold(Future.successful(BadRequest("Invalid timestamp"))) { case (from, until) =>
          executor.fetchHistory(from, until)
            .map(_.toMap.mapValues(_.status.name).toJSON)
            .map(Ok(_))
            .map(jsonContentType)
        }
      }.recoverWith(errorHandler)
    }
    // Stats of the board
    case GET at url"/api/boards/$board/stats" => {
      val boardName = URLDecoder.decode(board, "UTF-8")
      stateService.stats(boardName)
        .map(_.mapValues(decimalFormat.format)).map(_.toJSON)
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
}
