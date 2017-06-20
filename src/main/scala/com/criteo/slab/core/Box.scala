package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable
import org.json4s.CustomSerializer
import org.json4s.JsonDSL._
import shapeless.{HList, LUBConstraint}

/** A box that groups checks
  *
  * @param title       The title
  * @param checks      The checks
  * @param aggregate   Aggregates the views of its checks, return a view
  * @param description The description of the box in markdown syntax
  * @param labelLimit  The limit of visible check labels shown on the box
  */
case class Box[C <: HList](
                            title: String,
                            checks: C,
                            aggregate: (Map[Check[_], View], Context) => View,
                            description: Option[String] = None,
                            labelLimit: Option[Int] = None
                          )(
                            implicit constraints: LUBConstraint[C, Check[_]]
                          )


object Box {
  implicit object toJSON extends Jsonable[Box[_]] {
    override val serializers = List(Ser)

    object Ser extends CustomSerializer[Box[_]](_ => ( {
      case _ => throw new NotImplementedError("Not deserializable")
    }, {
      case box: Box[_] =>
        ("title" -> box.title) ~ ("description" -> box.description) ~ ("labelLimit" -> box.labelLimit.getOrElse(64))
    }
    ))

  }
}