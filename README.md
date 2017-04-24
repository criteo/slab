# Slab

A framework for creating monitoring dashboards.

## Getting started

To create a Slab dashboard, you need to define the following components

- Check

  A check declares a metric value to be checked, and how a value is interpreted as a view (status, message, etc.)

  - Example

  ```scala
  val check = Check[Latency](
    "id",
    "a title",
    () => HttpClient("http://remote/metric"),
    (latency, context) => if (latency.underlying > 500) View(Status.Error, "High latency") else View(Status.Success, "Ok")
  )
  ```

- Box

  A box groups multiple checks, the status of a box is derived from its children's views.

  - Example

  ```scala
  val box = Box(
    "box title",
    List(check1, check2),
    views => views.sorted.reverse.head,
    Some(
      """
        |# Description
        |> You can add some markdown description here
      """.stripMargin)
  )

  ```

- Board

  A board is a top-level element of Slab, which consists of a collection of boxes. It has takes a layout definitions of boxes. The board's global status is derived from its children boxes' views.

  - Example

  ```scala
  val board = Board(
    "board title",
    List(box1, box2),
    views => views.sorted.reverse.head,
    layout = Layout(
      List(
        Column(
          percentage = 50,
          List(
            Row(percentage = 100, List(box1))
          )
        ),
        Column(
          50,
          List(
            Row(percentage = 100, List(box2))
          )
        )
      )
    ),
    links = List(
      box1 -> box2
    )
  )
  ```

- WebServer

  To launch a Slab instance with the Web interface, just import `com.criteo.slab.app.WebServer`, initialize it with a list of boards, and launch it with a specified port.

## Contribution guide

  - Scala

    We created an example project to facilitate the development process, you can launch `com.criteo.slab.example.Launcher` from the `example` project.

  - Web app

    `src/main/webapp` contains all web application code and resources.

    - Install npm packages

      `npm install`

    - Start web dev server

      `npm run serve -- --env.port=$SERVER_PORT`

      where `SERVER_PORT` should be the port of Slab web server instance
