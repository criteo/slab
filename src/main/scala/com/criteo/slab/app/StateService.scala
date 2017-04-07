package com.criteo.slab.app

import java.util.concurrent.{Executors, TimeUnit}

import com.criteo.slab.core.{Board, BoardView}
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext

class StateService(
                    val boards: Seq[Board],
                    intervalSeconds: Int
                  )(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  // history in (board name -> (timestamp -> view tree))
  private val history = TrieMap.empty[String, TrieMap[Long, BoardView]]
  // current state in (board name -> view tree)
  private val current = TrieMap.empty[String, BoardView]

  private lazy val scheduler = Executors.newSingleThreadScheduledExecutor()

  def start(): Unit = {
    logger.info(s"started, checking interval: $intervalSeconds seconds")
    scheduler.scheduleAtFixedRate(Poller, 0, intervalSeconds, TimeUnit.SECONDS)
    initHistory()
  }

  private def initHistory(): Unit = {
    boards foreach { board =>
      board
        .fetchHistory(DateTime.now.minusDays(1), DateTime.now)
        .foreach { m =>
          history.get(board.title) match {
            case Some(value) => value ++= m
            case None =>
              history += board.title -> (TrieMap.empty ++= m)
          }
        }
    }
  }

  def getCurrent(name: String): Option[BoardView] = {
    current.get(name)
  }

  def getHistory(name: String): Option[Map[Long, BoardView]] = {
    history.get(name).map(_.toMap)
  }

  sys.addShutdownHook {
    logger.info(s"shutting down StateService")
    scheduler.shutdown()
  }

  object Poller extends Runnable {
    override def run(): Unit = {
      val checkTime = DateTime.now().withMillisOfSecond(0).withSecondOfMinute(0)
      logger.info(s"updating ${checkTime.toString()} (${checkTime.getMillis})")
      boards foreach { board =>
        board
          .apply(None)
          .foreach { viewTree =>
            current += board.title -> viewTree
            history.get(board.title) match {
              case Some(records) =>
                records += checkTime.getMillis -> viewTree
                // evict old entries
                // TODO: define retention time
                val obsoleted = records.keys.filter(_ < checkTime.minusDays(1).getMillis)
                if (obsoleted.size > 0) records --= obsoleted
                logger.info(s"updating history cache, size: ${records.size}")
              case None => history += board.title -> TrieMap(checkTime.getMillis -> viewTree)
            }
          }
      }
    }
  }

}
