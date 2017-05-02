package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable
import org.json4s.Serializer

/** Serializable view to be used by the web app
  *
  */
private[slab] trait ReadableView {
  val title: String
  val message: String
  val status: Status

  def asView = View(status, message)
}

private[slab] object ReadableView {

  implicit object ToJSON extends Jsonable[ReadableView] {
    override val serializers: Seq[Serializer[_]] = implicitly[Jsonable[Status]].serializers
  }
}

private[slab] case class BoardView(
                      title: String,
                      status: Status,
                      message: String,
                      boxes: Seq[BoxView]
                    ) extends ReadableView

private[slab] case class BoxView(
                    title: String,
                    status: Status,
                    message: String,
                    checks: Seq[CheckView]
                  ) extends ReadableView

private[slab] case class CheckView(
                      title: String,
                      status: Status,
                      message: String,
                      label: Option[String] = None
                    ) extends ReadableView
