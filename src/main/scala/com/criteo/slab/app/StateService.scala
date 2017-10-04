package com.criteo.slab.app

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.{Executors, TimeUnit}

import com.criteo.slab.core.{BoardView, Executor, Status}
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scalacache.caffeine.CaffeineCache

private[slab] class StateService(
                                  val executors: Seq[Executor[_]],
                                  val interval: Int,
                                  val statsDays: Int = 7
                                )(implicit ec: ExecutionContext) {

  import StateService._

  import scalacache._
  import scalacache.memoization._

  private val logger = LoggerFactory.getLogger(this.getClass)

  private implicit val scalaCache = ScalaCache(CaffeineCache())

  private lazy val scheduler = Executors.newSingleThreadScheduledExecutor()

  def start(): Unit = {
    scheduler.scheduleAtFixedRate(Poller, 0, interval, TimeUnit.SECONDS)
  }

  // Current board view
  def current(board: String): Future[BoardView] = get[BoardView, NoSerialization](board) flatMap {
    case Some(boardView) => Future.successful(boardView)
    case None => Future.failed(NotFoundError(s"$board does not exist"))
  }

  // All available board views
  def all(): Future[Seq[BoardView]] =
    Future
      .sequence(executors.map { e => get[BoardView, NoSerialization](e.board.title) })
      .map(_.collect { case Some(boardView) => boardView })

  // History of last 24 hours
  def history(board: String): Future[Map[Long, String]] = memoize(Duration.create(10, TimeUnit.MINUTES)) {
    logger.info(s"Updating history of $board")
    val now = Instant.now
    executors.find(_.board.title == board)
      .fold(Future.failed(NotFoundError(s"$board does not exist")): Future[Map[Long, String]]) {
        _
          .fetchHistory(now.minus(1, ChronoUnit.DAYS), now)
          .map(_.map { case (ts, view) => (ts, view.status.name) }.toMap)
      }
  }

  // Stats of last n days
  def stats(board: String): Future[Map[Long, Stats]] = memoize(Duration.create(12, TimeUnit.HOURS)) {
    logger.info(s"Updating statistics of $board")
    val now = Instant.now()
    executors.find(_.board.title == board)
      .fold(Future.failed(NotFoundError(s"$board does not exist")): Future[Map[Long, Stats]]) {
        _
          .fetchHistory(now.minus(statsDays, ChronoUnit.DAYS), now)
          .map(getStatsByHour)
      }
  }

  sys.addShutdownHook {
    logger.info("Shutting down...")
    scheduler.shutdown()
  }

  object Poller extends Runnable {
    override def run(): Unit = executors foreach { e =>
      e.apply(None).foreach(put(e.board.title)(_))
      history(e.board.title)
      stats(e.board.title)
    }
  }

}

object StateService {

  case class NotFoundError(message: String) extends Exception(message)

  // get aggregated statistics by hour
  def getStatsByHour(history: Seq[(Long, BoardView)]): Map[Long, Stats] = {
    history
      .groupBy { case (ts, _) =>
        // normalize to the start of hour
        ts - ts % 3600000
      }
      .mapValues { entries =>
        val (successes, warnings, errors, unknown, total) = entries.foldLeft((0, 0, 0, 0, 0)) { case ((successes, warnings, errors, unknown, total), (_, view)) =>
          view.status match {
            case Status.Success => (successes + 1, warnings, errors, unknown, total + 1)
            case Status.Warning => (successes, warnings + 1, errors, unknown, total + 1)
            case Status.Error => (successes, warnings, errors + 1, unknown, total + 1)
            case Status.Unknown => (successes, warnings, errors, unknown + 1, total + 1)
            case _ => (successes, warnings, errors, unknown, total)
          }
        }
        Stats(
          successes,
          warnings,
          errors,
          unknown,
          total
        )
      }
  }
}
