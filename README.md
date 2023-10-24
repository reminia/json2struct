# json2struct

Convert between json and Golang struct, use json4s as the json AST.

## json to struct

Use openai response as an example:

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

After convert:

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

TBD
