package json2struct

import json2struct.Printer.Syntax.toStringOps
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class PrinterSuite extends AnyWordSpec {

  "Printer" when {
    "using StringOps" should {
      "return upper camel case" in {
        "hello_world".upperCamelCase shouldBe "HelloWorld"
        "hello".upperCamelCase shouldBe "Hello"
      }

      "return snake case" in {
        "HelloWorld".snakeCase shouldBe "hello_world"
        "helloWorld".snakeCase shouldBe "hello_world"
        "helloWWorld".snakeCase shouldBe "hello_wworld"
        "hello".snakeCase shouldBe "hello"
        "Hello".snakeCase shouldBe "hello"
      }
    }
  }

}
