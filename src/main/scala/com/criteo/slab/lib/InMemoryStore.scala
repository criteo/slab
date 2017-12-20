package com.criteo.slab.lib

import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.temporal.ChronoUnit
import java.time.{Instant, ZoneId}
import java.util.concurrent.{Executors, TimeUnit}

import com.criteo.slab.core.{Codec, Context, Store}
import com.criteo.slab.lib.Values.Slo
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.util.Try

/** In memory store
  *
  * Entries are kept in memory, the store checks and removes expired entries periodically
  *
  * @param expiryDays The number of days for entries to be kept in the store since inserted, expired entries will be removed
  */
class InMemoryStore(
                     val expiryDays: Int = 30
                   ) extends Store[Any] {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val cache = TrieMap.empty[(String, Long), Any]
  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  scheduler.scheduleAtFixedRate(InMemoryStore.createCleaner(cache, expiryDays, logger), 1, 1, TimeUnit.HOURS)
  logger.info(s"InMemoryStore started, entries expire in $expiryDays days")

  sys.addShutdownHook {
    logger.info(s"Shutting down...")
    scheduler.shutdown()
  }

  override def upload[T](id: String, context: Context, v: T)(implicit codec: Codec[T, Any]): Future[Unit] = {
    logger.debug(s"Uploading $id")
    Future.successful {
      cache.putIfAbsent((id, context.when.toEpochMilli), codec.encode(v))
      logger.info(s"Store updated, size: ${cache.size}")
    }
  }

  override def uploadSlo(id: String, context: Context, slo: Slo)(implicit codec: Codec[Slo, Any]): Future[Unit] = {
    upload[Slo](id, context, slo)
  }

  def fetchSloHistory(id: String, from: Instant, until: Instant)(implicit codec: Codec[Slo, Any]): Future[Seq[(Long, Slo)]] = {
    fetchHistory[Slo](id, from, until)(codec)
  }

  override def fetch[T](id: String, context: Context)(implicit codec: Codec[T, Any]): Future[Option[T]] = {
    logger.debug(s"Fetching $id")
    Future.successful {
      cache.get((id, context.when.toEpochMilli)) map { v =>
        codec.decode(v).get
      }
    }
  }

  override def fetchHistory[T](
                                id: String,
                                from: Instant,
                                until: Instant
                              )(implicit ev: Codec[T, Any]): Future[Seq[(Long, T)]] = {
    logger.debug(s"Fetching the history of $id from ${format(from)} until ${format(until)}, cache size: ${cache.size}")
    Future.successful {
      cache withFilter { case ((_id, ts), _) =>
        _id == id && ts >= from.toEpochMilli && ts <= until.toEpochMilli
      } map { case ((_, ts), repr) =>
        (ts, ev.decode(repr).get)
      } toList
    }
  }

  private def format(i: Instant) = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
    .withZone(ZoneId.systemDefault)
    .format(i)
}

object InMemoryStore {
  implicit def codec[T] = new Codec[T, Any] {
    override def encode(v: T): Any = v

    override def decode(v: Any): Try[T] = Try(v.asInstanceOf[T])
  }

  def createCleaner(cache: TrieMap[(String, Long), Any], expiryDays: Int, logger: Logger): Runnable = {
    object C extends Runnable {
      override def run(): Unit = {
        val expired = cache.filterKeys(_._2 <= Instant.now.minus(expiryDays, ChronoUnit.DAYS).toEpochMilli).keys
        logger.debug(s"${expired.size} out of ${cache.size} entries have expired, cleaning up...")
        cache --= expired
      }
    }
    C
  }
}