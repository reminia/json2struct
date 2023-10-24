package json2struct

import org.json4s._
import org.json4s.native.JsonMethods._

object JsonReader {
  def read(json: String): JValue = parse(json)
}
