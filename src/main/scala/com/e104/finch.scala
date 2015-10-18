package com.e104

import com.twitter.finagle.Httpx
import com.twitter.finagle.httpx.Response
import com.twitter.util.Await
import io.finch.request.{RequestReader, paramOption , body ,param ,_ }
import io.finch.response.Ok
import io.finch.route.{Router, get, _}
import io.finch.json4s._
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers

/**
 * Created by kenny.lee on 2015/10/12.
 */
object finch extends App {

  //json
  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  val urldata: RequestReader[(String, String, Int)] =
    (paramOption("x").as[String] :: paramOption("y").as[String] :: paramOption("z").as[Int]).asTuple.map {
      case (x, y, z) => (x.getOrElse(""), y.getOrElse(""),z.getOrElse(0))
    }

  //http://localhost:8080/one
  val api1: Router[Response] = get("one") { Ok("foo") }

  //http://localhost:8080/two
  val api2: Router[String] = get("two") { "bar" }

  //http://localhost:8080/three?x="ss"&y="hh"&z=3
  val api3: Router[String] = get("three" ? urldata)((urldata: (String, String, Int)) => s"Hello " + urldata._1 + "," + urldata._2 + "," + urldata._3)

  //http://localhost:8080/four/kenny
  val api4: Router[String] =
    get("four" / string)((x: String) => s"Hello, $x")

 // val api5 : Router[String] =
 //   post("data" ? urldata) {(urldata: (String, String, Int)) => s"Hello " + urldata._1 + "," + urldata._2 + "," + urldata._3}
 case class Bar (x: Int, y: String)
  //json get output
  //http://localhost:8080/five/kenny?x="132"&y="456"&z=1
 val okres: Response = Ok(Bar(1, "hello"))
 val api6:Router[Response] = get("five" / string ? urldata) {(x : (String) , urldata: ( String, String, Int)) => {
   println(x + ":" + urldata._1 + ":" + urldata._2 + ":" + urldata._3)
   Ok(Bar(1, urldata._1))} }

  //json post input & output
  // http://localhost:8080/six
  // post data {"x":1,"y":"xyz"}
  val foo: RequestReader[Bar] = body.as[Bar]
  val api7:Router[Response] = post("six" ? foo) { (postdata:Bar) => { Ok(postdata) }  }

  //url param with condition
  //http://localhost:8080/seven?x=19&y=test
  val user: RequestReader[Bar] = (
        param("x").as[Int].shouldNot(beLessThan(18)) ::
        param("y")
    ).as[Bar]

  val api8:Router[Response] = get("seven" ? user) { (urldata:Bar) => {
    println(urldata.x + ":" + urldata.y)
    Ok(Bar(1, urldata.y))} }


  val allroute = api1 :+: api2 :+: api3 :+: api4 :+: api6  :+: api7  :+: api8
  Await.ready(Httpx.serve(":8080", allroute.toService))
}
