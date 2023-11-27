# api

A simple rest [api](src/main/scala/json2struct/api/Server.scala) that servers the conversion between json data and struct type.

3 endpoints:

1. `POST /v1/convert/json`, convert json to struct. The request body is json format,
 like {"name": "RootType", "json":"xxxx"}.
2. `POST /v1/convert/struct`, convert struct to json. The request body is plain text of struct types you want to convert.
3. `GET /health`, query server healthy status.
4. `POST /v2/convert/json?name=xxx`, convert json to struct. Query name is the root struct type name,
request body is plain json data. It's easy to use than the v1 one.
5. `POST /v2/convert/struct`, convert struct to json. The request body is plain struct types and a new header **config**
is added to support converter configuration. The header is optional.

Try it with below curl commands:

```bash
curl http://localhost:8080/health

curl -X POST -d 'type Person struct {
  Name string
  Age int
  FavoriteMovie string
}' http://localhost:8080/v1/convert/struct

curl -X POST -H "Content-Type: application/json"  -d '{
  "name": "Root",
  "json": "{\"usage\":{\"prompt_tokens\":5,\"completion_tokens\":7,\"total_tokens\":12}}"}' \
  http://localhost:8080/v1/convert/json

curl -X POST -d '{
    "usage": {
      "prompt_tokens": 5,
      "completion_tokens": 7,
      "total_tokens": 12
    }
  }' http://localhost:8080/v2/convert/json?name=Root

curl -X POST -H "Content-Type: application/json" \
-H 'config: {"struct2json.snake-case": true}' \
-d 'type Person struct {
  Name string
  Age int
  FavoriteMovie string
}' http://localhost:8080/v2/convert/struct
```

## Docker
Docker image is built using the sbt-native-packager/docker plugin.
* Run `sbt docker:publishLocal` to build the image.
* Start the container by `docker run -d  -p 8081:8080 ghcr.io/reminia/json2struct-api`.
* Or use the one I have published to gcr by `docker pull ghcr.io/reminia/json2struct-api`.

Latest version is up-to-date with the newest code.
