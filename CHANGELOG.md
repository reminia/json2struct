# Changelog

## 0.1.0
* core logic of json and struct pair conversion

## 0.2.0
* add single line and multiline comment parser

## 0.3.0-SNAPSHOT
* refactor to multi-module project
* add rest api
* zip api and cli as artifact
* add a docker image for api

## 0.3.0
* fix bug for single struct conversion
* not generate the field if json tag is '-'

## 0.4.0 
* unknown type to any type
* convert snake case name to upper camel case
* remove redundant Field.Struct type
* struct to json should lower the first letter of json key
* add snake-case conf to support converting to snake-case style json props
* add a new v2 json2struct endpoint with name as query parameter and json as body
* support to release latest docker image based on newest code in github ci
* add a new v2 struct2json endpoint to support converter customization with config header
* add scalafmt conf

## 0.5.0 
* minor code optimization
* test api with sbt-curl plugin
* add readme for core module 
* support struct type with comment(line & multiline)

## 0.6.0 
* support nested struct type
```golang
type Student struct {
  Name string
  Age int     
  Address struct {
    Home string  
    Office string
  }
}
```

## 0.7.0
* aws lambda deployment support