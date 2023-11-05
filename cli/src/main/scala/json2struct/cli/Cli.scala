package json2struct.cli

import json2struct.Converter
import json2struct.Printer.Syntax.toPrinterOps
import org.rogach.scallop.ScallopConf


object Cli {
  class Conf(args: Seq[String]) extends ScallopConf(args) {
    version("[json2struct/0.1.0]")
    banner(
      """Usage: json2struct -j root "json content"
        |or  json2struct "struct content"
        |""".stripMargin)
    val json = opt[String](name = "json", short = 'j', descr = "json name")
    val content = trailArg[String](descr = "input json or struct", required = true)
    verify()
  }

  def main(args: Array[String]): Unit = {
    val conf = new Conf(args)
    val content = conf.content()
    val json = conf.json
    if (json.isSupplied) {
      val name = json()
      Converter
        .convertJson(content, name)
        .map(_.print())
        .foreach(println)
    } else {
      Converter
        .convertStruct(content)
        .map(_.print())
        .foreach(println)
    }
  }
}