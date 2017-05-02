package com.criteo.slab.lib

import com.criteo.slab.utils.Jsonable
import org.json4s.JsonAST.{JArray, JDouble, JInt, JNull}
import org.json4s.{CustomSerializer, Serializer}

private[slab] case class DataPoint(value: Option[Double], timestamp: Long)

object DataPoint {

  implicit object ToJSON extends Jsonable[DataPoint] {
    override val serializers: Seq[Serializer[_]] = List(Ser)

    object Ser extends CustomSerializer[DataPoint](_ => ( {
      case JArray(JDouble(value) :: JInt(date) :: Nil) =>
        DataPoint(Some(value), date.toLong)
      case JArray(JNull :: JInt(date) :: Nil) =>
        DataPoint(None, date.toLong)
    }, {
      case DataPoint(value, date) =>
        val v = value match {
          case Some(v) => JDouble(v)
          case None => JNull
        }
        JArray(
          List(
            v,
            JInt(date)
          )
        )
    }
    ))

  }

}

private[slab] case class GraphiteMetric(
                           target: String,
                           datapoints: List[DataPoint]
                         )

object GraphiteMetric {

  implicit object ToJSON extends Jsonable[GraphiteMetric] {
    override val serializers: Seq[Serializer[_]] = implicitly[Jsonable[DataPoint]].serializers
  }

}
