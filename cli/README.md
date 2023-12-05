# cli

It's a simple cli tool.

* `bin/cli --help`, show help message.
* `bin/cli "struct content"`, convert struct to random json data.
* `bin/cli -j name "json content"`, convert json to struct types,
 name is the top struct type name.

 Try it with:

 ```bash
bin/cli -j Root '{"usage": {"prompt_tokens": 5, "completion_tokens": 7, "total_tokens": 12}}'

bin/cli 'type Person struct {
   Name string
   Age int
   FavoriteMovie string
}'
 ```
