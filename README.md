# json2struct

Convert between json and Golang struct, use json4s as the json AST.

## Motivation

It's needed frequently to convert between http request/response body and struct types in Golang.
So I write this tool to reduce some boilerplate handwrite work.

## json to struct

Json before:

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

Struct after:

```golang
type OpenAiResponse struct {
    Model string     `json:"model"`
    Choices []Choices     `json:"choices"`
    Usage Usage     `json:"usage"`
    Object string     `json:"object"`
    Id string     `json:"id"`
    Created int     `json:"created"`
}
type Choices struct {
    Finish_reason string     `json:"finish_reason"`
    Index int     `json:"index"`
    Text string     `json:"text"`
    Logprobs Unknown     `json:"logprobs"`
}
type Usage struct {
    Completion_tokens int     `json:"completion_tokens"`
    Prompt_tokens int     `json:"prompt_tokens"`
    Total_tokens int     `json:"total_tokens"`
}
```

## struct to json

Struct before:

```golang
type OpenAiResponse struct {
    Model string     `json:"model,omitempty" xml:"model"`
    Choices []Choices     `json:"choices"`
    Usage Usage     `json:"usage"`
    Object string     `json:"object"`
    Id string     `json:"id"`
    Created int     `json:"created"`
}
```

Struct AST after:

```scala
Struct(OpenAiResponse,List(Simple(Model,string,Simple(Map(xml -> List(model), json -> List(model, omitempty)))), Array(Choices,Choices,Simple(Map(json -> List(choices)))), Struct(Usage,Simple(Map(json -> List(usage)))), Simple(Object,string,Simple(Map(json -> List(object)))), Simple(Id,string,Simple(Map(json -> List(id)))), Simple(Created,int,Simple(Map(json -> List(created)))))))
```

Random json generated:
TBD
