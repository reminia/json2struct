# cli

It's a simple [cli](cli/src/main/scala/json2struct/cli/Cli.scala) tool.
After packaging, you can use it as:

* `bin/json2struct --help`, show help message.
* `bin/json2struct "struct content"`. It converts struct to random json data.
* `bin/json2struct -j name "json content"`. It converts json to struct types,
 name is the top struct type name.
