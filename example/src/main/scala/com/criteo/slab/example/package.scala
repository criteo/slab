package com.criteo.slab

import com.criteo.slab.core.View

package object example {
  def takeHighestLevel(views: Seq[View]): View = views.sorted.reverse.head
}
