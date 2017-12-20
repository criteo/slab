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

  // A box for the web service, which contains latency checks
  lazy val webService = Box(
    "Webservice alpha",
    // The list of checks concerned in this box
    makeVersionCheck("web.service.version", "Version", 1.2, Status.Success, Some("V1.2")) :: Nil,
    // A function that aggregates the views of the children checks
    takeMostCritical[Version],
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
    List(
      makeRandomLatencyCheck("gateway.beta.eu", "EU Gateway latency"),
      makeRandomLatencyCheck("gateway.beta.us", "US Gateway latency")
    ),
    takeMaxLatency
  )

  lazy val pipelineZeta = Box(
    "Pipeline Zeta",
    List(
      makeRandomLatencyCheck("pipeline.zeta.a", "Job A latency"),
      makeRandomLatencyCheck("pipeline.zeta.b", "Job B latency"),
      makeRandomLatencyCheck("pipeline.zeta.c", "Job C latency")
    ),
    takeMaxLatency
  )

  lazy val pipelineOmega = Box(
    "Pipeline Omega",
    List(
      makeRandomLatencyCheck("pipeline.omega.a", "Job A latency", Some("Top Job")),
      makeRandomLatencyCheck("pipeline.omega.b", "Job B latency"),
      makeRandomLatencyCheck("pipeline.omega.c", "Job C latency"),
      makeRandomLatencyCheck("pipeline.omega.d", "Job D latency"),
      makeRandomLatencyCheck("pipeline.omega.e", "Job E latency")
    ),
    takeMaxLatency,
    // Limit the number of labels shown on the box
    labelLimit = Some(3)
  )

  lazy val databaseKappa = Box(
    "Database Kappa",
    List(
      makeRandomLatencyCheck("database.kappa.dc1", "DC1 Latency"),
      makeRandomLatencyCheck("database.kappa.dc2", "DC2 Latency")
    ),
    takeMaxLatency
  )

  lazy val ui = Box(
    "User interface",
    makeVersionCheck("ui.version", "Version 1000", 1000, Status.Success) :: Nil,
    takeMostCritical[Version],
    labelLimit = Some(0)
  )

  // Define the layout of the board
  lazy val layout = Layout(
    // Define 3 columns, each takes 33.3% of the width of the board
    Column(
      33.3,
      // We put the web server box in this row
      Row("Tier 1", 100, Seq(webService))
    ),
    Column(
      33.3,
      // Define two rows, each row takes 50% of the height of the column
      Row("Tier 2 - 1", 50, Seq(gateway)),
      Row("Tier 2 - 2", 50, Seq(pipelineZeta, pipelineOmega))
    ),
    Column(
      33.3,
      Row("Tier 3", 100, Seq(databaseKappa, ui))
    )
  )

  // Create a board
  lazy val board = Board(
    "Simple board",
    webService :: gateway :: pipelineZeta :: pipelineOmega :: databaseKappa :: ui :: HNil,
    (views, _) => views.maxBy(_._2)._2,
    layout,
    // Declare the links between boxes, which be shown in the UI
    Seq(webService -> gateway, gateway -> pipelineZeta, pipelineZeta -> databaseKappa)
  )

  // A function that aggregates the views of the children checks
  def takeMostCritical[T](views: Map[Check[T], CheckResult[T]], ctx: Context): View = views.maxBy(_._2.view)._2.view.copy(message = "")

  // Aggregate the latency values and display the max latency
  def takeMaxLatency(views: Map[Check[Latency], CheckResult[Latency]], ctx: Context): View = {
    val maxLatency = views.map(_._2.value.map(_.underlying)).flatten.foldLeft(0L)(Math.max)
    View(
      views.maxBy(_._2.view)._2.view.status,
      s"max latency $maxLatency ms"
    )
  }

  val versionFormatter = new DecimalFormat("##.###")

  // A mock check generator for version values
  def makeVersionCheck(id: String, title: String, value: Double, status: Status, label: Option[String] = None) = Check[Version](
    id,
    title,
    () => Future.successful(Version(value)),
    display = (v: Version, _: Context) => View(status, s"version ${versionFormatter format v.underlying}", label)
  )

  // A mock check generator for latency values
  def makeRandomLatencyCheck(id: String, title: String, label: Option[String] = None) = Check[Latency](
    id,
    title,
    () => Future.successful(Latency(Random.nextInt(1000))),
    display = (l: Latency, _: Context) => {
      val status = if (l.underlying >= 990) {
        Status.Error
      } else if (l.underlying >= 980) {
        Status.Warning
      } else {
        Status.Success
      }
      // A view represents the result of a check, such as status, message and label, the message can be in Markdown
      View(status, s"latency **${l.underlying}** ms", label)
    }
  )
}
