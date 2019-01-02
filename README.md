[![Maven](https://img.shields.io/maven-central/v/com.github.vanbv/num.svg)](https://repo.maven.apache.org/maven2/com/github/vanbv/num/)
[![License](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=PMQ36YX3ST6WN&source=url)
[![Language](https://img.shields.io/badge/Language-Russian-blue.svg)](README.ru-RU.md)
# num
num (Netty URL Mapping) is a Java library that adds request mapping to the Netty library.
## Getting Started
Add dependency to start using num library:
```groovy
compile group: 'com.github.vanbv', name: 'num', version: '0.0.1', ext: 'jar'
```
Besides num library you need to add [Netty](https://netty.io/) library to the project.
## Guide
To handle a request, you must create a handler class. This class will handle incoming http requests to the server. For that end you need to extends class AbstractHttpMappingHandler, create the method that returns FullHttpResponse and annotate it as:
* `@Get`
* `@Post`
* `@Put`
* `@Delete`
> Only GET, POST, PUT and DELETE requests are supported now.

It is necessary to specify the URL-address in the `value` parameter of the annotation. The method is called referring to URL-address.
```java
@Get("/test/get")
public DefaultFullHttpResponse get() {
…
}
```
You can use annotations to pass parameters in methods:
* `@PathParam` - for path parameters
```java
@Get("/test/get/{message}")
public DefaultFullHttpResponse get(@PathParam(value = "message") String message) {
…
}
```
* `@QueryParam` - for query parameters
```java
@Get("/test/get")
public DefaultFullHttpResponse get(@QueryParam(value = "message") String message) {
…
}
```
* `@RequestBody` - for `POST` requests body. It is allowed to use only in `POST` requests.
```java
@Post("test/post")
public DefaultFullHttpResponse post(@RequestBody Message message) String message) {
…
}
```
To use `@RequestBody`, you must pass the implementation of the`JsonParser` interface, which parses data from the request body, to the constructor of the handler class. The library already has a default implementation of `JsonParserDefault`. The implementation uses a `jackson` parser. Therefore, if you use this implementation, you must add a dependency to the project `com.fasterxml.jackson.core:jackson-databind`.
## License
Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
## Donation
To support the project please click the button.

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=PMQ36YX3ST6WN&source=url)
