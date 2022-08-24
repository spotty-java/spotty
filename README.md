# Spotty Web Server

Spotty is a fast, lightweight java web server with a simple API and rich functionality.

## Getting started

maven dependency
```xml
<dependency>
  <groupId>spotty</groupId>
  <artifactId>server</artifactId>
  <version>1.0.0</version>
</dependency>
```

gradle dependency
```groovy
dependencies {
    implementation "spotty:server:1.0.0"
}
```

## Code Example

Hello world example:
```java
import spotty.Spotty;

public class App {
    public static void main(String[] args) {
        final Spotty spotty = new Spotty();
        spotty.start();

        spotty.get("/hello", (request, response) -> "Hello World");
    }
}
```

If you need to wait for when the server will be started, you can use `awaitUntilStart`
```java
spotty.start();
spotty.awaitUntilStart();

// your code
```

To stop the server:
```java
spotty.stop();
```

To wait until the server will be stopped:
```java
spotty.stop();
spotty.awaitUntilStop();

// your code
```

## Routes
The main code block of Spotty web server is based on the route. The route is the entry point of a request to the server.

Within a route, Spotty web server supports these HTTP methods:
```java
spotty.get("/", (request, response) -> {
    // your code
});

spotty.post("/", (request, response) -> {
    // your code
});

spotty.put("/", (request, response) -> {
    // your code
});

spotty.patch("/", (request, response) -> {
    // your code
});

spotty.delete("/", (request, response) -> {
    // your code
});

spotty.head("/", (request, response) -> {
    // your code
});

spotty.trace("/", (request, response) -> {
    // your code
});

spotty.connect("/", (request, response) -> {
    // your code
});

spotty.options("/", (request, response) -> {
    // your code
});
```

The route path template can include named parameters which are accessible via the `param()` method on the request object
```java
spotty.get("/hello/:name", (request, response) -> {
    return "Hello " + request.param("name");
});
```

The route path template can also include wildcard parameters
```java
spotty.get("/hello/:name/say/*/something", (request, response) -> {
    return "Hello " + request.param("name");
});
```

You can restrict your route by accept-type:
```java
spotty.get("/hello", "application/json", (request, response) -> "Hello JSON");
spotty.get("/hello", "application/xml", (request, response) -> "Hello XML");
```

## Delete route
A route can be removed by the `removeRoute` function
```java
import spotty.common.http.HttpMethod.*;

spotty.clearRoutes(); // remove all routes
spotty.removeRoute("/route/path"); // remove all routes with path /route/path 
spotty.removeRoute("/route/path", GET); // remove all routes with path /route/path and http method GET
spotty.removeRoute("/route/path", "application/json", GET); // remove route with path /route/path, application/json accept-type and http method GET
```

## Path groups
Routes can be grouped, and this is very useful when you have a lot of them. 
To do this, call the `path()` method. This method takes a String prefix, and offers the capability to be able to declare routes, nested paths, and filters inside it.
```java
spotty.path("/api", () -> {
    spotty.before("/*", (req, res) -> log.info("got request before filter"));
    
    spotty.path("/email", () -> {
        spotty.post("/add", Email::add);
        spotty.put("/update", Email::update);
        spotty.delete("/delete", Email::delete);
    });
    
    spotty.after("/*", (req, res) -> log.info("got request after filter"));
});
```

## Request
Request information is provided by request object:
```java
request.protocol(); // the protocol, e.g. HTTP/1.1
request.scheme();
request.method();
request.path();
request.contentLength();
request.contentType();
request.host();
request.ip();
request.port();
request.cookies();
request.headers();
request.params();
request.param(String name);
request.queryParamsMap();
request.queryParams();
request.queryParams(String name);
request.queryParam(String name);
request.attach(Object attachment);
request.attachment();
request.session();
request.body();
```