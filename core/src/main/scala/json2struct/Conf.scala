package json2struct

import com.typesafe.config.{Config, ConfigFactory}

import java.nio.file.Paths

object Conf {
  val HOME = sys.env.getOrElse("JSON2STRUCT_HOME", "/opt/json2struct")
  val APP_CONF = loadConf().resolve()

  def loadConf(): Config = {
    val file = Paths.get(HOME).resolve("conf/app.conf").toFile
    ConfigFactory.parseFile(file)
      .withFallback(ConfigFactory.load("app.conf"))
      .withFallback(ConfigFactory.load("app-default.conf"))
      .withFallback(ConfigFactory.load())
  }

  def snakeCaseEnabled: Boolean = APP_CONF.getBoolean("json2struct.snake-case")
}
