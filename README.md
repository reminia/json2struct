# json2struct ![ci](https://github.com/reminia/json2struct/actions/workflows/scala.yml/badge.svg)

Convert between json and Golang struct, using json4s as the json AST and scalacheck for random data
generation.

## Motivation

It's needed frequently to convert between http request/response body and struct types in Golang.

## Features

* json to go struct type
* struct type to random json data
* support json tag in go struct
* support both snake case & camel case of field name
* support parsing struct type with comment
* rest api
* cli tool

## Build & Run

* setup GITHUB_REPOSITORY_OWNER and GITHUB_TOKEN env variable to access github package registry.
* `sbt compile test`, compile and test it.
* `sbt universal:packageBin`, package zip which lies in target/universal folder.
* unzip the [zip](https://github.com/reminia/json2struct/releases) package,
  run it by `bin/cli` or `bin/api` script.

## Modules

* [core](core/README.md), the core conversion logic.
* [cli](cli/README.md), a simple cli tool that wraps the conversion logic, a zip is released for this module.
* [api](api/README.md), a simple rest api that serves the conversion, a zip and a docker image are released for direct usage.

## json2struct

Json will be parsed to a sequence of struct types annotated with json tags.
Below is the sample json and the generated struct types:

```json
{
  "id": "cmpl-uqkvlQyYK7bGYrRHQ0eXlWi7",
  "object": "text_completion",
  "created": 1589478378,
  "model": "gpt-3.5-turbo",
  "choices": [
    {
      "text": "\n\nThis is indeed a test",
      "index": 0,
      "logprobs": null,
      "finish_reason": "length"
    }
  ],
  "usage": {
    "prompt_tokens": 5,
    "completion_tokens": 7,
    "total_tokens": 12
  }
}
```

```golang
type OpenAiResponse struct {
    Id  string  `json:"id"`
    Object  string  `json:"object"`
    Created  int  `json:"created"`
    Model  string  `json:"model"`
    Choices  []Choices  `json:"choices"`
    Usage  Usage  `json:"usage"`
}
type Choices struct {
    Text  string  `json:"text"`
    Index  int  `json:"index"`
    Logprobs  any  `json:"logprobs"`
    FinishReason  string  `json:"finish_reason"`
}
type Usage struct {
    PromptTokens  int  `json:"prompt_tokens"`
    CompletionTokens  int  `json:"completion_tokens"`
    TotalTokens  int  `json:"total_tokens"`
}
```

## struct2json

Struct definitions will be parsed to a sequence of Struct [AST](core/src/main/scala/json2struct/GoStructAST.scala).
Each AST can be converted to a map filled with random data.
Below is the sample struct types and generated json data:

```golang
type OpenAiResponse struct {
	Id      string   `json:"id"`
	Object  string   `json:"object"`
	Created uint64   `json:"created"`
	Model   string   `json:"model"`
	Choices []Choice `json:"choices"`
	Usage   Usage    `json:"usage"`
}
type Choice struct {
	Index        int     `json:"index"`
	Message      Message `json:"message"`
	FinishReason string  `json:"finish_reason"`
}
type Usage struct {
	PromptTokens     int `json:"prompt_tokens"`
	CompletionTokens int `json:"completion_tokens"`
	TotalTokens      int `json:"total_tokens"`
}

type Message struct {
	Role    string `json:"role"`
	Content string `json:"content"`
}
```

```json
{"model":"QC","choices":[{"index":482,"message":{"role":"cQUu2Gd","content":"NL"},"finish_reason":"Lckyp"},{"index":8,"message":{"role":"reH6","content":"o"},"finish_reason":"yMgu"},{"index":704,"message":{"role":"5X","content":"wXVmgIN"},"finish_reason":"mNl8"}],"usage":{"prompt_tokens":906,"completion_tokens":569,"total_tokens":930},"object":"5Y2li","id":"HSQ9","created":6840891044428693685}
```
