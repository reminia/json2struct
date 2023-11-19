package json2struct

import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class PrinterTest extends AnyWordSpec {

  "Printer" should {
    "return snake case" in {
      Printer.snakeCase("HelloWorld") shouldBe "hello_world"
      Printer.snakeCase("helloWorld") shouldBe "hello_world"
      Printer.snakeCase("helloWWorld") shouldBe "hello_wworld"
      Printer.snakeCase("hello") shouldBe "hello"
      Printer.snakeCase("Hello") shouldBe "hello"
    }
  }

}
