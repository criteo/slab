package com.criteo.slab.app

import java.time.{Instant, ZoneOffset, ZonedDateTime}
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
                                  val statsDays: Int = 730
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

  def stats(board: String): Future[Map[Long, Double]] = {
    val now = ZonedDateTime.now(ZoneOffset.UTC)
    executors.find(_.board.title == board)
      .fold(Future.failed(NotFoundError(s"$board does not exist")): Future[Map[Long, Double]]) {
        executor =>

          def getSloCacheDuration(from: ZonedDateTime): Duration =
            if (from.plus(1, ChronoUnit.MONTHS).compareTo(now) > 0)
            // expire cache fro current month more often
              Duration.create(1, TimeUnit.HOURS)
            else
            // don't expire all caches in the same time
              Duration.create(12, TimeUnit.HOURS).plus(Duration.create((Math.random() * 30).toInt, TimeUnit.MINUTES))

          //cache only full months
          def boardMonthlySloInner(board: String, from: ZonedDateTime, until: ZonedDateTime): Future[Map[Long, Double]] = memoize(getSloCacheDuration(from)) {
            executor.fetchHourlySlo(from.toInstant, until.toInstant).map(_.toMap)
          }

          val from = now.minus(statsDays, ChronoUnit.DAYS)
          val until = now

          //extend the interval with one day in both directions to avoid timezone issues
          val fromStartOfMonth = from.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)
          val months = ChronoUnit.MONTHS.between(fromStartOfMonth, until.plus(1, ChronoUnit.DAYS)).toInt

          val monthlyResults =
            for {idx <- 0 to months} yield {
              val from = fromStartOfMonth.plus(idx, ChronoUnit.MONTHS)
              val to = fromStartOfMonth.plus(idx + 1, ChronoUnit.MONTHS)
              boardMonthlySloInner(board, from, to)
            }
          val fromTs = from.toEpochSecond * 1000
          val toTs = until.toEpochSecond * 1000
          Future.sequence(monthlyResults).map(_.fold(Map.empty[Long, Double])(_ ++ _).filterKeys(ts => ts >= fromTs && ts < toTs))
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
