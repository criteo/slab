package com.criteo.slab.app

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.{Executors, TimeUnit}

import com.criteo.slab.core.{Board, BoardView, Status}
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scalacache._
import scalacache.caffeine.CaffeineCache
import scalacache.memoization._

/** A service for the app state
  *
  * @param boards          The boards
  * @param intervalSeconds The number of seconds for polling interval
  * @param statsDays       The number of days of the statistics to calculate, defaults to 7
  * @param ec              The execution context
  */
private[slab] class StateService(
                                  val boards: Seq[Board],
                                  val intervalSeconds: Int,
                                  val statsDays: Int = 7
                                )(implicit ec: ExecutionContext) {

  import StateService._

  private val logger = LoggerFactory.getLogger(this.getClass)

  private implicit val scalaCache = ScalaCache(CaffeineCache())

  private lazy val scheduler = Executors.newSingleThreadScheduledExecutor()

  // Current board view
  def current(board: String): Future[BoardView] = get[BoardView, NoSerialization](board) flatMap {
    case Some(boardView) => Future.successful(boardView)
    case None => Future.failed(NotFoundError(s"$board is not ready"))
  }

  // Stats of last n days
  def stats(board: String): Future[Map[Long, Stats]] = memoize(Duration.create(1, TimeUnit.DAYS)) {
    logger.info(s"Updating statistics of $board")
    val now = Instant.now
    boards
      .find(_.title == board)
      .fold(Future.failed(NotFoundError(s"$board does not exist")): Future[Map[Long, Stats]]) {
        _
          .fetchHistory(now.minus(statsDays, ChronoUnit.DAYS), now)
          .map(getStatsByDay)
      }
  }

  // History of last 24 hours
  def history(board: String): Future[Map[Long, String]] = memoize(Duration.create(10, TimeUnit.MINUTES)) {
    logger.info(s"Updating history of $board")
    val now = Instant.now
    boards
      .find(_.title == board)
      .fold(Future.failed(NotFoundError(s"$board does not exist")): Future[Map[Long, String]]) {
        _
          .fetchHistory(now.minus(1, ChronoUnit.DAYS), now)
          .map(_.mapValues(_.status.name))
      }
  }

  // Start the service
  def start(): Unit = {
    scheduler.scheduleAtFixedRate(Poller, 0, intervalSeconds, TimeUnit.SECONDS)
  }

  sys.addShutdownHook {
    logger.info("Shutting down...")
    scheduler.shutdown()
  }

  object Poller extends Runnable {
    override def run(): Unit = {
      boards foreach { board =>
        board
          .apply(None)
          .foreach(put(board.title)(_))
        history(board.title)
        stats(board.title)
      }
    }
  }
}

object StateService {

  case class NotFoundError(message: String) extends Exception(message)

  def getStatsByDay(history: Map[Long, BoardView]): Map[Long, Stats] = {
    history
      .groupBy { case (ts, _) =>
        ts - ts % 86400000 // normalize to the start of the day
      }
      .mapValues { entries =>
        val (successes, warnings, errors, total) = entries.foldLeft((0, 0, 0, 0)) { case ((successes, warnings, errors, total), (_, view)) =>
          view.status match {
            case Status.Success => (successes + 1, warnings, errors, total + 1)
            case Status.Warning => (successes, warnings + 1, errors, total + 1)
            case Status.Error => (successes, warnings, errors + 1, total + 1)
            case Status.Unknown => (successes, warnings, errors, total + 1)
          }
        }
        Stats(
          successes,
          warnings,
          errors,
          total
        )
      }
  }
}
