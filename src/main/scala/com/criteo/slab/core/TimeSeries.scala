package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable
import org.json4s.JsonAST.{JArray, JDouble, JLong}
import org.json4s.{CustomSerializer, Serializer}


case class Point(
                  data: Double,
                  timestamp: Long
                )

object Point {
  implicit object ToJSON extends Jsonable[Point] {
    override val serializers: Seq[Serializer[_]] = Seq(Ser)

    object Ser extends CustomSerializer[Point](_ => (
      {
        case JArray(JDouble(data)::JLong(timestamp)::_) => Point(data, timestamp)
      },
      {
        case Point(data, timestamp) =>
          JArray(JDouble(data)::JLong(timestamp)::Nil)
      }
    ))
  }
}

case class TimeSeries(
                       title: String,
                       data: List[Point]
                     )

object TimeSeries {

  implicit object ToJSON extends Jsonable[TimeSeries] {
    override val serializers: Seq[Serializer[_]] = Jsonable[Point].serializers
  }

  implicit object ToJSONArray extends Jsonable[Seq[TimeSeries]] {
    override val serializers: Seq[Serializer[_]] = Jsonable[TimeSeries].serializers
  }
}
