// Example: A simple Slab board
//
// Guide for creating a Slab board
package com.criteo.slab.example


import java.text.DecimalFormat

import com.criteo.slab.core._
import com.criteo.slab.lib.Values.{Latency, Version}

import scala.concurrent.Future
import scala.util.Random

object SimpleBoard {

  // A box for the web server, which contains a version check and a latency check
  lazy val webserver = Box(
    // Define the title of the box
    "Webserver Alpha",
    Seq(
      // The list of checks concerned in this box
      makeVersionCheck("web.version", "Version", 1.2, Status.Success, Some("V1.2")),
      makeRandomLatencyCheck("web.latency", "Latency")
    ),
    // A function that aggregates the views of the children checks
    takeMostCritical,
    // You can write a description for the box (supports Markdown)
    Some(
      """
        |# Doc for web server (markdown)
        |<br/>
        |## URL
        |- http://example.io
        |- http://10.0.0.1
      """.stripMargin)
  )

  lazy val gateway = Box(
    "Gateway Beta",
    Seq(
      makeRandomLatencyCheck("EU", "EU Gateway latency"),
      makeRandomLatencyCheck("US", "US Gateway latency")
    ),
    takeMostCritical
  )

  lazy val pipelineZeta = Box(
    "Pipeline Zeta",
    Seq(
      makeRandomLatencyCheck("A", "Job A latency"),
      makeRandomLatencyCheck("B", "Job B latency"),
      makeRandomLatencyCheck("C", "Job C latency")
    ),
    takeMostCritical
  )

  lazy val pipelineOmega = Box(
    "Pipeline Omega",
    Seq(
      makeRandomLatencyCheck("A", "Job A latency"),
      makeRandomLatencyCheck("B", "Job B latency"),
      makeRandomLatencyCheck("C", "Job C latency"),
      makeRandomLatencyCheck("D", "Job D latency"),
      makeRandomLatencyCheck("E", "Job E latency")
    ),
    takeMostCritical,
    // Limit the number of labels shown on the box
    labelLimit = Some(3)
  )

  lazy val databaseKappa = Box(
    "Database Kappa",
    Seq(
      makeRandomLatencyCheck("DC1", "DC1 Latency"),
      makeRandomLatencyCheck("DC2", "DC2 Latency")
    ),
    takeMostCritical
  )

  lazy val ui = Box(
    "User interface",
    Seq(
      makeVersionCheck("ui.version", "Version 1000", 1000, Status.Success)
    ),
    takeMostCritical,
    labelLimit = Some(0)
  )

  // Define the layout of the board
  lazy val simpleBoardLayout = Layout(
    // Define 3 columns, each takes 33.3% of the width of the board
    Seq(Column(
      33.3,
      // We put the web server box in this row
      Seq(Row("Tier 1", 100, Seq(webserver)))
    ), Column(
      33.3,
      // Define two rows, each row takes 50% of the height of the column
      Seq(
        Row("Tier 2 - 1", 50, Seq(gateway)),
        Row("Tier 2 - 2", 50, Seq(pipelineZeta, pipelineOmega))
      )
    ), Column(
      33.3,
      Seq(
        Row("Tier 3", 100, Seq(databaseKappa, ui))
      )
    ))
  )

  // Define a board creator: takes a value store (can be implicit) and returns a board
  def apply()(implicit valueStore: ValueStore) = Board(
    // The title of the board
    "Simple board",
    Seq(webserver, gateway, pipelineZeta, pipelineOmega, databaseKappa, ui),
    takeMostCritical,
    simpleBoardLayout,
    Seq(webserver -> gateway, gateway -> pipelineZeta, pipelineZeta -> databaseKappa)
  )

  // Example aggregate function:
  // takes the view with the most critical status except the unknown
  def takeMostCritical(views: Seq[View]): View = views.filter(_.status != Status.Unknown).sorted.reverse.head

  // A check generator for demonstration:
  // generates a random latency
  def makeRandomLatencyCheck(id: String, title: String, label: Option[String] = None) = Check(
    id,
    title,
    // For each check, you should define a function that returns a value wrapped in future
    () => Future.successful(
      Latency(Random.nextInt(1000))
    ),
    // A function that takes the checked value and the context, returns a view
    display = (l: Latency, _: Context) => {
      val status = if (l.underlying >= 990) {
        Status.Error
      } else if (l.underlying >= 980) {
        Status.Warning
      } else {
        Status.Success
      }
      // A view contains information like status, message and label
      View(status, s"latency ${l.underlying}ms", label)
    }
  )

  val versionFormatter = new DecimalFormat("##.###")

  // A mock check generator for "Version" values
  def makeVersionCheck(id: String, title: String, value: Double, status: Status, label: Option[String] = None) = Check(
    id,
    title,
    () => Future.successful(Version(value)),
    display = (_: Version, _: Context) => View(status, s"version ${versionFormatter format value}", label)
  )
}
