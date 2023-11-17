# cli

It's a simple [cli](src/main/scala/json2struct/cli/Cli.scala) tool.
After packaging, you can use it as:

* `bin/cli --help`, show help message.
* `bin/cli "struct content"`. It converts struct to random json data.
* `bin/cli -j name "json content"`. It converts json to struct types,
 name is the top struct type name.
