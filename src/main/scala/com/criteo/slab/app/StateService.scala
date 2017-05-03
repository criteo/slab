package com.criteo.slab.app

import java.time.Instant
import java.time.temporal.ChronoUnit.{DAYS, MINUTES}
import java.util.concurrent.{Executors, TimeUnit}

import com.criteo.slab.core.{Board, BoardView, ReadableView, Status}
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext
import com.criteo.slab.utils.Jsonable._

/** A service for the app state
  *
  * @param boards The boards
  * @param intervalSeconds The number of seconds for polling interval
  * @param statsDays The number of days of the statistics to calculate, defaults to 7
  * @param ec The execution context
  */
private[slab] class StateService(
                    val boards: Seq[Board],
                    val intervalSeconds: Int,
                    val statsDays: Int = 7
                  )(implicit ec: ExecutionContext) {

  import StateService._

  private val logger = LoggerFactory.getLogger(this.getClass)

  // history in (board name -> (timestamp -> view tree))
  private val history = TrieMap.empty[String, TrieMap[Long, BoardView]]
  // history in (board name -> JSON string)
  private val historyJSON = TrieMap.empty[String, String]
  // current state in (board name -> view tree)
  private val current = TrieMap.empty[String, BoardView]
  // stats in (board name -> (timestamp -> Stats))
  private val stats = TrieMap.empty[String, TrieMap[Long, Stats]]

  private lazy val scheduler = Executors.newSingleThreadScheduledExecutor()

  def start(): Unit = {
    logger.info(s"started, checking interval: $intervalSeconds seconds")
    scheduler.scheduleAtFixedRate(Poller, 0, intervalSeconds, TimeUnit.SECONDS)
    loadHistory()
    loadStats()
  }

  // Load the history of last 24 hours
  private def loadHistory(): Unit = {
    val now = Instant.now
    boards foreach { board =>
      board
        .fetchHistory(now.minus(1, DAYS), now)
        .foreach { entries =>
          history.get(board.title) match {
            case Some(value) =>
              value ++= entries
              historyJSON += board.title -> (entries:Map[Long, ReadableView]).toJSON
            case None =>
              history += board.title -> (TrieMap.empty ++= entries)
          }
        }
    }
  }

  // Load the stats of last n days
  private def loadStats(): Unit = {
    val now = Instant.now
    boards foreach { board =>
      logger.info(s"loading stats for ${board.title}")
      board
        .fetchHistory(now.minus(statsDays, DAYS), now)
        .map(getStatsByDay)
        .foreach { newStats =>
          stats.get(board.title) match {
            case Some(value) =>
              value ++= newStats
            case None =>
              stats += board.title -> (TrieMap.empty ++= newStats)
          }
        }
    }
  }

  def getCurrent(name: String): Option[BoardView] = {
    current.get(name)
  }

  def getHistory(name: String): Option[String] = {
    historyJSON.get(name)
  }

  def getStats(name: String): Option[Map[Long, Stats]] = {
    stats.get(name).map(_.toMap)
  }

  object Poller extends Runnable {
    override def run(): Unit = {
      val checkTime = Instant.now().truncatedTo(MINUTES)
      logger.info(s"updating ${checkTime.toString()} (${checkTime.toEpochMilli})")
      boards foreach { board =>
        board
          .apply(None)
          .foreach { viewTree =>
            // Update the current view tree of the board
            current += board.title -> viewTree
            // Update history cache
            val records = history.getOrElseUpdate(board.title, TrieMap(checkTime.toEpochMilli -> viewTree)) += checkTime.toEpochMilli -> viewTree
            // evict old entries
            val obsoleted = records.keys.filter(_ < checkTime.minus(1, DAYS).toEpochMilli)
            if (obsoleted.size > 0) {
              logger.debug(s"evicted ${obsoleted.size} history entries")
              records --= obsoleted
            }
            historyJSON += board.title -> records.toMap[Long, ReadableView].toJSON
            logger.debug(s"history cache updated, new size: ${records.size}")
            // Update stats
            stats
              .getOrElseUpdate(board.title, TrieMap(checkTime.toEpochMilli -> Stats(0, 0, 0, 0)))
              .get(checkTime.toEpochMilli)
              .map(updateStatsWithStatus(_, viewTree.status))
          }
      }
    }
  }

  sys.addShutdownHook {
    logger.info(s"shutting down StateService")
    scheduler.shutdown()
  }
}

object StateService {
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

  def updateStatsWithStatus(stats: Stats, status: Status) = {
    status match {
      case Status.Success => stats.copy(successes = stats.successes + 1, total = stats.total + 1)
      case Status.Warning => stats.copy(warnings = stats.warnings + 1, total = stats.total + 1)
      case Status.Error => stats.copy(errors = stats.errors + 1, total = stats.total + 1)
      case Status.Unknown => stats.copy(total = stats.total + 1)
    }
  }
}
