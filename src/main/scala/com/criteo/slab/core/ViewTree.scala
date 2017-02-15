package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, DefaultFormats, Extraction, Serializer}

sealed trait ViewTree {
  val title: String
  val view: View
}

case class ViewLeaf(title: String, view: View) extends ViewTree

case class ViewNode(title: String, view: View, children: Seq[ViewTree]) extends ViewTree

object ViewTree {
  implicit object ToJSON extends Jsonable[ViewTree] {
    implicit val formats = DefaultFormats + Ser
    override val serializers: Seq[Serializer[_]] = List(Ser)

    object Ser extends CustomSerializer[ViewTree](format => (
      {
        case JObject(JField("title", JString(title))::JField("status", JString(status))::JField("message", JString(message))::Nil) =>
          ViewLeaf(title, View(Status.from(status), message))
        case JObject(JField("title", JString(title))::JField("status", JString(status))::JField("message", JString(message))::JField("children", JArray(c))::Nil) =>
          ViewNode(title, View(Status.from(status), message), c.map(Extraction.extract[ViewTree](_)))
      },
      {
        case v: ViewNode =>
          ("title" -> v.title) ~ ("status" -> v.view.status.name) ~ ("message" -> v.view.message) ~ ("children" -> v.children.map(Extraction.decompose))
        case v: ViewLeaf =>
          ("title" -> v.title) ~ ("status" -> v.view.status.name) ~ ("message" -> v.view.message)
      }
    ))
  }
}