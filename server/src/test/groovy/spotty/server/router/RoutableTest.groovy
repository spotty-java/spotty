package spotty.server.router

import spock.lang.Specification
import spotty.common.exception.SpottyException
import spotty.common.exception.SpottyHttpException
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
import static spotty.common.http.HttpStatus.NOT_FOUND

class RoutableTest extends Specification {

    private def routable = new Routable()

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

        routable.addRoute("/hello", GET, get)
        routable.addRoute("/hello/*/world", POST, post)
        routable.addRoute("/hello/:id/:category/world", PUT, put)
        routable.addRoute("/api/*", PATCH, patch)
        routable.addRoute("/*", DELETE, delete)
        routable.addRoute("/hello", HEAD, head)
        routable.addRoute("/hello", TRACE, trace)
        routable.addRoute("/hello", CONNECT, connect)
        routable.addRoute("/hello", OPTIONS, options)

        when:
        var getFound = routable.getRoute("/hello", GET)
        var postFound = routable.getRoute("/hello/no/world", POST)
        var putFound = routable.getRoute("/hello/1/car/world", PUT)
        var patchFound = routable.getRoute("/api/v1/product", PATCH)
        var deleteFound = routable.getRoute("/any/path", DELETE)
        var headFound = routable.getRoute("/hello", HEAD)
        var traceFound = routable.getRoute("/hello", TRACE)
        var connectFound = routable.getRoute("/hello", CONNECT)
        var optionsFound = routable.getRoute("/hello", OPTIONS)

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

        routable.addRoute("/*", GET, route1)
        routable.addRoute("/hello", GET, route2)
        routable.addRoute("/hello/*/world", GET, route3)

        when:
        var route1Found = routable.getRoute("/any/path", GET)
        var route2Found = routable.getRoute("/hello", GET)
        var route3Found = routable.getRoute("/hello/any/world", GET)

        then:
        route1 == route1Found.route
        route2 == route2Found.route
        route3 == route3Found.route
    }

    def "should sort templates from longest to shortest"() {
        given:
        var routes = ["/hello/*/world", "/hello-world", "/hello/*/*", "/hello"]

        when:
        routable.addRoute("/hello/:user/:name", GET, {})
        routable.addRoute("/hello", POST, {})
        routable.addRoute("/hello-world", POST, {})
        routable.addRoute("/hello/*/world", POST, {})

        then:
        routes == routable.sortedList.toNormalizedPaths()
    }

    def "should throw an error when path is duplicated"() {
        when:
        routable.addRoute(path1, GET, {})
        routable.addRoute(path2, GET, {})

        then:
        thrown SpottyException

        where:
        path1                         | path2
        "/hello"                      | "/hello"
        "/product/:id/name"           | "/product/*/name"
        "/product/:id/name/*"         | "/product/*/name/*"
        "/product/:id/name/:category" | "/product/*/name/*"
    }

    def "should throw an exception when route not found"() {
        given:
        routable.addRoute("/hello", GET, {})
        routable.addRoute("/hello", POST, {})

        when:
        routable.getRoute("/hello", DELETE)

        then:
        var e = thrown SpottyHttpException
        e.status == NOT_FOUND
    }

}
