package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable
import org.json4s.JsonAST.JString
import org.json4s.{CustomSerializer, DefaultFormats}
import org.json4s.jackson.Serialization

import scala.util.Try

case class Column(title: String, percentage: Int, rows: List[Row])

case class Row(percentage: Int, boxes: List[Box])

case class Layout(columns: Seq[Column])

object Layout {
  implicit val json = new Jsonable[Layout] {
    implicit val formats = DefaultFormats + BoxFormats

    override def parse(in: String): Try[Layout] = Try(Serialization.read(in))

    override def serialize(in: Layout): String = Serialization.write(in)

    object BoxFormats extends CustomSerializer[Box](_ => (
      {
        // broken deserializer, normally we never need to deserialize it
        case JString(title) => Box(title, Seq.empty, vs => vs.head)
      },
      {
        case box: Box => JString(box.title)
      }
    ))
  }
}
