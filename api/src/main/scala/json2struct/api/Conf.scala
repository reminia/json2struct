package json2struct.api

import com.typesafe.config.{Config, ConfigFactory}

object Conf {
  val conf = loadConf().resolve()

  def loadConf(): Config = {
    ConfigFactory.load("app.conf")
      .withFallback(ConfigFactory.load("app-default.conf"))
      .withFallback(ConfigFactory.load())
  }

  val HttpPort = conf.getInt("api.http-port")
}
