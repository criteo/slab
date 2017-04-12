package com.criteo.slab.core

import org.joda.time.DateTime

import scala.concurrent.Future

trait ValueStore {
  def upload(id: String, values: Map[String, Double]): Future[Unit]
  def fetch(id: String, context: Context): Future[Map[String, Double]]
  def fetchHistory(id: String, from: DateTime, until: DateTime): Future[Map[Long, Metrical.Out]]
}

object NoopValueStore extends ValueStore {
  override def upload(id: String, values: Map[String, Double]): Future[Unit] = Future.successful(())

  override def fetch(id: String, context: Context): Future[Map[String, Double]] = Future.successful(Map.empty)

  override def fetchHistory(id: String, from: DateTime, until: DateTime): Future[Map[Long, Metrical.Out]] = Future.successful(Map.empty)
}