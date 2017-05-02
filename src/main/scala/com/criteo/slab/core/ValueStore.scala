package com.criteo.slab.core

import java.time.Instant

import scala.concurrent.Future

/** A store provides a service to upload and fetch metrics
  *
  */
trait ValueStore {
  def upload(id: String, values: Map[String, Double]): Future[Unit]

  def fetch(id: String, context: Context): Future[Map[String, Double]]

  def fetchHistory(id: String, from: Instant, until: Instant): Future[Map[Long, Metrical.Out]]
}
