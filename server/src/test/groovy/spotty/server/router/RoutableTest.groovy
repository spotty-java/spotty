package spotty.server.router

import spock.lang.Specification
import spotty.common.exception.SpottyException
import spotty.server.router.route.Route

import static spotty.common.http.HttpMethod.CONNECT
import static spotty.common.http.HttpMethod.DELETE
import static spotty.common.http.HttpMethod.GET
import static spotty.common.http.HttpMethod.HEAD
import static spotty.common.http.HttpMethod.OPTIONS
import static spotty.common.http.HttpMethod.PATCH
import static spotty.common.http.HttpMethod.POST
import static spotty.common.http.HttpMethod.PUT
import static spotty.common.http.HttpMethod.TRACE

class RoutableTest extends Specification {

    private def container = new Routable()

    def "should find routes correctly"() {
        given:
        var Route get = {}
        var Route post = {}
        var Route put = {}
        var Route patch = {}
        var Route delete = {}
        var Route head = {}
        var Route trace = {}
        var Route connect = {}
        var Route options = {}

        container.addRoute("/hello", GET, get)
        container.addRoute("/hello/*/world", POST, post)
        container.addRoute("/hello/:id/:category/world", PUT, put)
        container.addRoute("/api/*", PATCH, patch)
        container.addRoute("/*", DELETE, delete)
        container.addRoute("/hello", HEAD, head)
        container.addRoute("/hello", TRACE, trace)
        container.addRoute("/hello", CONNECT, connect)
        container.addRoute("/hello", OPTIONS, options)

        when:
        var getFound = container.getRoute("/hello", GET)
        var postFound = container.getRoute("/hello/no/world", POST)
        var putFound = container.getRoute("/hello/1/car/world", PUT)
        var patchFound = container.getRoute("/api/v1/product", PATCH)
        var deleteFound = container.getRoute("/any/path", DELETE)
        var headFound = container.getRoute("/hello", HEAD)
        var traceFound = container.getRoute("/hello", TRACE)
        var connectFound = container.getRoute("/hello", CONNECT)
        var optionsFound = container.getRoute("/hello", OPTIONS)

        then:
        get == getFound.route
        post == postFound.route
        put == putFound.route
        patch == patchFound.route
        delete == deleteFound.route
        head == headFound.route
        trace == traceFound.route
        connect == connectFound.route
        options == optionsFound.route
    }

    def "should find most suitable route"() {
        given:
        var Route route1 = {}
        var Route route2 = {}
        var Route route3 = {}

        container.addRoute("/*", GET, route1)
        container.addRoute("/hello", GET, route2)
        container.addRoute("/hello/*/world", GET, route3)

        when:
        var route1Found = container.getRoute("/any/path", GET)
        var route2Found = container.getRoute("/hello", GET)
        var route3Found = container.getRoute("/hello/any/world", GET)

        then:
        route1 == route1Found.route
        route2 == route2Found.route
        route3 == route3Found.route
    }

    def "should return not found route"() {
        given:
        container.addRoute("/hello", GET, {})
        container.addRoute("/hello", POST, {})
        container.addRoute("/hello-world", POST, {})

        when:
        var notFoundPath = container.getRoute("/unknown-path", GET)
        var notFoundMethod = container.getRoute("/hello", DELETE)

        then:
        notFoundPath == null
        notFoundMethod == null
    }

    def "should sort templates from longest to shortest"() {
        given:
        var routes = ["/hello/*/world", "/hello-world", "/hello/*/*", "/hello"]

        when:
        container.addRoute("/hello/:user/:name", GET, {})
        container.addRoute("/hello", POST, {})
        container.addRoute("/hello-world", POST, {})
        container.addRoute("/hello/*/world", POST, {})

        then:
        routes == container.sortedList.toNormalizedPaths()
    }

    def "should throw an error when path is duplicated"() {
        when:
        container.addRoute(path1, GET, {})
        container.addRoute(path2, GET, {})

        then:
        thrown SpottyException

        where:
        path1                         | path2
        "/hello"                      | "/hello"
        "/product/:id/name"           | "/product/*/name"
        "/product/:id/name/*"         | "/product/*/name/*"
        "/product/:id/name/:category" | "/product/*/name/*"
    }
}
