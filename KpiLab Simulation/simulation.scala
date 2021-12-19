package GoRest

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Random {
  //val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
  def randomString(length: Int) = scala.util.Random.alphanumeric.take(length).mkString

  def randomInt(max: Int): Int ={
    val random = new scala.util.Random().nextInt(max)
    random
  }

  def randomAuthor() : String =
    """{"id":"""".stripMargin + Random.randomInt(999) + """",
         |"idBook":"""".stripMargin + Random.randomInt(50) + """",
         |"firstName":"""".stripMargin + Random.randomString(25) + """",
         |"lastName":"""".stripMargin + Random.randomString(25) + """"}""".stripMargin
}

class RequestSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("https://fakerestapi.azurewebsites.net")

  val getAuthors = scenario("Get Authors")
    .exec(
      http("Get Author by ID")
        .get("/api/v1/Authors"))
    .pause(1);

  val post_getAuthor = scenario("Post/Get Author by ID")
    .exec(session => {
      val sessionRes = session.set("postrequest", Random.randomAuthor())
      sessionRes
    })
    .exec(
      http("Post Author")
        .post("/api/v1/Authors")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.id").saveAs("Id"))
    )
    .pause(1)
    .exitHereIfFailed
    .exec(
      http("Get Author")
        .get("/api/v1/Authors/${Id}")
    )
    .pause(1);

  val putAuthor = scenario("Put Books")
    .exec(session => {
          val sessionRes = session.set("postrequest", Random.randomAuthor())
          sessionRes
        })
    .exec(http("Post Author")
      .post("/api/v1/Authors")
      .body(StringBody("${postrequest}")).asJson
      .check(jsonPath("$.id").saveAs("Id"))
    )
    .exitHereIfFailed
    .exec(session => {
          val sessionRes = session.set("putrequest", Random.randomAuthor())
          sessionRes
        })
    .exec(http("Put Author")
      .put("/api/v1/Authors/${Id}")
      .body(StringBody("${putrequest}")).asJson)
      .pause(1);

  val deleteAuthor = scenario("Delete Author")
    .exec(session => {
          val sessionRes = session.set("putrequest", Random.randomAuthor())
          sessionRes
        })
    .exec(http("Put Author")
      .post("/api/v1/Authors/")
      .body(StringBody("${putrequest}")).asJson
      .check(jsonPath("$.id").saveAs("Id"))
    )
    .exitHereIfFailed
    .exec(http("Delete Author").delete("/api/v1/Authors/${Id}"))

  setUp(getAuthors.inject(rampUsers(50).during(10.seconds)).protocols(httpProtocol),
        post_getAuthor.inject(rampUsers(50).during(10.seconds)).protocols(httpProtocol),
        deleteAuthor.inject(rampUsers(50).during(10.seconds)).protocols(httpProtocol),
        putAuthor.inject(rampUsers(50).during(10.seconds)).protocols(httpProtocol))
}