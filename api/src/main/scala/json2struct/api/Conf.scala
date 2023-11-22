package json2struct.api

import json2struct.Conf.APP_CONF

object Conf {
  val HttpPort = APP_CONF.getInt("api.http-port")
}
