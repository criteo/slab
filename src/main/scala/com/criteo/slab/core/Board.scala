package com.criteo.slab.core

import shapeless.{HList, UnaryTCConstraint}

/** Top level component
  *
  * @param title     The board title
  * @param boxes     The children boxes
  * @param aggregate Aggregates its children boxes views
  * @param layout    The layout of the board
  * @param links     Defines links between boxes, will draw lines in the UI
  */
case class Board[B <: HList](
                              title: String,
                              boxes: B,
                              aggregate: (Map[Box[_], View], Context) => View,
                              layout: Layout,
                              links: Seq[(Box[_], Box[_])] = Seq.empty
                            )(
                              implicit constraint: UnaryTCConstraint[B, Box]
                            )
