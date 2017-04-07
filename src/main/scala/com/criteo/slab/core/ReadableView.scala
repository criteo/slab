package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable
import org.json4s.Serializer

trait ReadableView {
  val title: String
  val message: String
  val status: Status

  def asView = View(status, message)
}

case class BoardView(
                      title: String,
                      status: Status,
                      message: String,
                      boxes: Seq[BoxView]
                    ) extends ReadableView

case class BoxView(
                    title: String,
                    status: Status,
                    message: String,
                    checks: Seq[CheckView]
                  ) extends ReadableView

case class CheckView(
                      title: String,
                      status: Status,
                      message: String,
                      label: Option[String] = None
                    ) extends ReadableView

object ReadableView {

  implicit object ToJSON extends Jsonable[ReadableView] {
    override val serializers: Seq[Serializer[_]] = implicitly[Jsonable[Status]].serializers
  }

}
