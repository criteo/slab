package com.criteo.slab.lib

import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.{Instant, ZoneId}

import com.criteo.slab.core.{Context, Store, Codec}
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.util.Try

/**
  * In memory store
  */
class InMemoryStore extends Store[Any] {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val cache = TrieMap.empty[(String, Long), Any]

  override def upload[T](id: String, context: Context, v: T)(implicit codec: Codec[T, Any]): Future[Unit] = {
    logger.debug(s"Uploading $id")
    Future.successful {
      cache.putIfAbsent((id, context.when.toEpochMilli), codec.encode(v))
      logger.info(s"Store updated, size: ${cache.size}")
    }
  }

  override def fetch[T](id: String, context: Context)(implicit codec: Codec[T, Any]): Future[T] = {
    logger.debug(s"Fetching $id")
    Future.successful {
      cache.get((id, context.when.toEpochMilli)) match {
        case Some(v) => codec.decode(v).get
        case None => throw new NoSuchElementException(s"$id:${context.when.toEpochMilli * 1000} does not exist")
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
}