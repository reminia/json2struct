# api

A simple rest api that servers the conversion between json data and struct type.

3 endpoints:

1. `POST /v1/covert/json`, convert json to struct. The request body is json format,
   it is something like {"name": "RootType", "json":"xxxx"}.
2. `POST /v1/convert/struct`, convert struct to json. The reqeust body is plain text struct type you want to convert.
3. `GET /health`, server healthy status query.
