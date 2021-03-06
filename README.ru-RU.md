[![Maven](https://img.shields.io/maven-central/v/com.github.vanbv/num.svg)](https://repo.maven.apache.org/maven2/com/github/vanbv/num/)
[![License](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Language](https://img.shields.io/badge/Language-English-blue.svg)](README.md)
# num

num (Netty URL Mapping) - это библиотека на Java, добавляющая в библиотеку [Netty](https://netty.io/) функционал по маппингу запросов.

## Быстрый старт
Для подключения библиотеки num в проект с использованием Gradle необходимо добавить следующую зависимость:
```groovy
compile group: 'com.github.vanbv', name: 'num', version: '0.0.3', ext: 'jar'
```
Кроме самой библиотеки num к проекту также требуется подключить библиотеку [Netty](https://netty.io/).
## Руководство
Для обработки запроса необходимо создать класс-обработчик. Этот класс будет заниматься обработкой входящих на сервер http-запросов. Для этого нужно отнаследоваться от класса `AbstractHttpMappingHandler`, создать метод, который будет возвращать `FullHttpResponse` и навешать на него одну из следующих аннотаций:
* `@Get`
* `@Post`
* `@Put`
* `@Delete`
> На данный момент поддерживаются только `GET`, `POST`, `PUT` и `DELETE` запросы.

В качестве параметра `value` в аннотации необходимо указать URL-адрес, при обращении на который будет вызываться данный метод.
```java
@Get("/test/get")
public DefaultFullHttpResponse get() {
…
}
```
Для передачи параметров в методы можно использовать следующие аннотации:
* `@PathParam` - для path-параметров
```java
@Get("/test/get/{message}")
public DefaultFullHttpResponse get(@PathParam(value = "message") String message) {
…
}
```
* `@QueryParam` - для query-параметров
```java
@Get("/test/get")
public DefaultFullHttpResponse get(@QueryParam(value = "message") String message) {
…
}
```
* `@RequestBody` - для тела `POST`-запросов. Разрешается использовать только в `POST`-запросах.
```java
@Post("test/post")
public DefaultFullHttpResponse post(@RequestBody Message message) String message) {
…
}
```
Для использования `@RequestBody` необходимо в конструктор класса-обработчика передать реализацию интерфейса `JsonParser`, которая будет заниматься парсингом данных из тела запроса. Также в библиотеке уже имеется реализация по умолчанию `JsonParserDefault`. В качестве парсера данная реализация использует `jackson`. Поэтому при использовании этой реализации необходимо подключить к проекту библиотеку `com.fasterxml.jackson.core:jackson-databind`.
## Демо
Посмотреть возможности библиотеки num можно в отдельном [проекте](https://github.com/vanbv/num-demo).
## Лицензия
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
