# api

A simple rest [api](src/main/scala/json2struct/api/Server.scala) that serves the conversion between json data and struct type.

Start it locally at 8080 with:
```bash
sbt
api / runMain json2struct.api.Server
```

## Http endpoints

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

# name is optional, default to Root
curl -X POST -d '{
    "usage": {
      "prompt_tokens": 5,
      "completion_tokens": 7,
      "total_tokens": 12
    }
  }' http://localhost:8080/v2/convert/json?name=Root

# convert struct to json with snake-case enabled
curl -X POST -H "Content-Type: application/json" \
-H 'config: {"struct2json.snake-case": true}' \
-d 'type Person struct {
  Name string
  Age int
  FavoriteMovie string
}' http://localhost:8080/v2/convert/struct

# convert nested struct type to json
curl -X POST -H "Content-Type: application/json" \
-H 'config: {"struct2json.snake-case": true}' \
-d 'type Student struct {
  Name string
  Age int
  Address struct {
    Home string
    Office string
  }
}' http://localhost:8080/v2/convert/struct
```

## Docker
Docker image is built using the sbt-native-packager/docker plugin.
* Run `sbt docker:publishLocal` to build the image or pull the image from gcr by `docker pull ghcr.io/reminia/json2struct-api`.
* Start the server by `docker run -d  -p 8080:8080 ghcr.io/reminia/json2struct-api`.

Latest version is up-to-date with the newest code.

## aws lambda
[Handler](src/main/scala/LambdaHandler.scala) is added for deployment to aws lambda.<br/>
Build the assembly jar by `sbt 'api/assembly'` and upload it to aws lambda code configuration.
