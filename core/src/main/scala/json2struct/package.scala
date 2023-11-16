package object json2struct {

  val UNKNOWN = "*Unknown*"

  // special json props in struct tag except name
  val SPECIAL_JSON_PROPS: Seq[String] = Seq("omitempty", "-")

  // go struct json tag
  val JSON_TAG = "json"
  // go json prop to ignore the field
  val JSON_IGNORE = "-"
}
