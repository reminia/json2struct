# json2struct ![ci](https://github.com/reminia/json2struct/actions/workflows/scala.yml/badge.svg)

Convert between json and Golang struct, using json4s as the json AST and scalacheck for random data
generation.

## Motivation

It's needed frequently to convert between http request/response body and struct types in Golang.

## json to struct

Json input:

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

Generated struct types:

```golang
type OpenAiResponse struct {
    Id    string    `json:"id"`
    Object    string    `json:"object"`
    Created    int    `json:"created"`
    Model    string    `json:"model"`
    Choices    []Choices    `json:"choices"`
    Usage    Usage    `json:"usage"`
}
type Choices struct {
    Text    string    `json:"text"`
    Index    int    `json:"index"`
    Logprobs    *Unknown*    `json:"logprobs"`
    Finish_reason    string    `json:"finish_reason"`
}
type Usage struct {
    Prompt_tokens    int    `json:"prompt_tokens"`
    Completion_tokens    int    `json:"completion_tokens"`
    Total_tokens    int    `json:"total_tokens"`
}
```

## struct to json

All structs will be parsed to a sequence of [Struct AST](src/main/scala/json2struct/GoStructAST.scala).
And then random data will be filled into the structs to produce fake json data.

Struct input:

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

Json generated:

```json
{"model":"QC","choices":[{"index":482,"message":{"role":"cQUu2Gd","content":"NL"},"finish_reason":"Lckyp"},{"index":8,"message":{"role":"reH6","content":"o"},"finish_reason":"yMgu"},{"index":704,"message":{"role":"5X","content":"wXVmgIN"},"finish_reason":"mNl8"}],"usage":{"prompt_tokens":906,"completion_tokens":569,"total_tokens":930},"object":"5Y2li","id":"HSQ9","created":6840891044428693685}
```
