# api

A simple rest [api](src/main/scala/json2struct/api/Server.scala) that servers the conversion between json data and struct type.

3 endpoints:

1. `POST /v1/convert/json`, convert json to struct. The request body is json format,
 like {"name": "RootType", "json":"xxxx"}.
2. `POST /v1/convert/struct`, convert struct to json. The request body is plain text of struct types you want to convert.
3. `GET /health`, query server healthy status.

## Docker
Docker image is built using the sbt-native-packager/docker plugin.
* Run `sbt docker:publishLocal` to build the image.
* Start the container by `docker run -d  -p 8081:8080 ghcr.io/reminia/json2struct-api:0.2`.
* Or use the one I have published to gcr by `docker pull ghcr.io/reminia/json2struct-api:0.2`.
