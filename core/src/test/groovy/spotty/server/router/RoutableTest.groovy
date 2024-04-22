package spotty.server.router

import spock.lang.Specification
import spotty.common.exception.SpottyHttpException
import spotty.common.exception.SpottyRouteDuplicationException
import spotty.common.router.route.Route

import static org.apache.http.entity.ContentType.APPLICATION_JSON
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
        get == getFound.route()
        post == postFound.route()
        put == putFound.route()
        patch == patchFound.route()
        delete == deleteFound.route()
        head == headFound.route()
        trace == traceFound.route()
        connect == connectFound.route()
        options == optionsFound.route()
    }

    def "should find routes with acceptType correctly"() {
        given:
        var Route getAll = {}
        var Route getJson = {}
        var Route getXml = {}
        var Route postXml = {}

        routable.addRoute("/hello/world", GET, getAll)
        routable.addRoute("/hello", GET, "application/json", getJson)
        routable.addRoute("/hello", GET, "application/xml", getXml)
        routable.addRoute("/hello/*/:name/:last_name", POST, "application/xml", postXml)

        when:
        var getAllFound = routable.getRoute("/hello/world", GET)
        var getJsonFound = routable.getRoute("/hello", GET, "application/json")
        var getXmlFound = routable.getRoute("/hello", GET, "application/xml")

        var postFound = routable.getRoute("/hello/you/john/doe", POST, "application/xml")
        var postXmlFound = routable.getRoute("/hello/you/john/doe", POST, "application/xml")

        then:
        getAll == getAllFound.route()
        getJson == getJsonFound.route()
        getXml == getXmlFound.route()
        postXml == postFound.route()
        postXml == postXmlFound.route()
    }

    def "should find route for any accept type"() {
        given:
        var Route get = {}
        routable.addRoute("/hello/*/:name", GET, get)

        when:
        var getFound = routable.getRoute("/hello/my/world", GET, acceptType)

        then:
        get == getFound.route()

        where:
        acceptType         | _
        "application/json" | _
        "application/xml"  | _
        "unregistered"     | _
        null               | _
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
        route1 == route1Found.route()
        route2 == route2Found.route()
        route3 == route3Found.route()
    }

    def "should throw an error when path is duplicated"() {
        when:
        routable.addRoute(path1, GET, {})
        routable.addRoute(path2, GET, {})

        then:
        thrown SpottyRouteDuplicationException

        where:
        path1                         | path2
        "/hello"                      | "/hello"
        "/product/:id/name"           | "/product/*/name"
        "/product/:id/name/*"         | "/product/*/name/*"
        "/product/:id/name/:category" | "/product/*/name/*"
    }

    def "should throw an exception when route not found by http method"() {
        given:
        routable.addRoute("/hello", GET, {})
        routable.addRoute("/hello", POST, {})

        when:
        routable.getRoute("/hello", DELETE)

        then:
        var e = thrown SpottyHttpException
        e.status == NOT_FOUND
    }

    def "should throw an exception when route not found by accept type"() {
        given:
        var Route get = {}
        routable.addRoute("/hello", GET, "application/json", get)

        when:
        var getFound = routable.getRoute("/hello", GET, "application/json")

        // should throw an exception
        routable.getRoute("/hello", GET, acceptType)

        then:
        get == getFound.route()
        var e = thrown SpottyHttpException
        e.status == NOT_FOUND

        where:
        acceptType        | _
        "application/xml" | _
        "unregistered"    | _
        null              | _
    }

    def "should remove route correctly"() {
        given:
        var Route post = {}
        routable.addRoute("/hello/:name", GET, {})
        routable.addRoute("/hello/:name", POST, post)

        when:
        // no exception
        routable.getRoute("/hello/bob", GET)
        var postFound = routable.getRoute("/hello/john", POST)

        var isRemoved = routable.removeRoute("/hello/:name")

        // should throw an exception
        routable.getRoute("/hello/alex", GET)

        then:
        isRemoved
        post == postFound.route()
        var e = thrown SpottyHttpException
        e.status == NOT_FOUND
    }

    def "should remove route by method correctly"() {
        given:
        var Route post = {}
        routable.addRoute("/hello/:name", GET, {})
        routable.addRoute("/hello/:name", POST, post)

        when:
        // no exception
        routable.getRoute("/hello/alex", GET)
        routable.getRoute("/hello/alex", POST)

        var isRemoved = routable.removeRoute("/hello/:name", GET)

        var routeFound = routable.getRoute("/hello/alex", POST)

        // should throw an exception
        routable.getRoute("/hello/alex", GET)

        then:
        isRemoved
        post == routeFound.route()
        var e = thrown SpottyHttpException
        e.status == NOT_FOUND
    }

    def "should remove by acceptType correctly"() {
        given:
        var Route post = {}
        routable.addRoute("/hello/:name", GET, APPLICATION_JSON.getMimeType(), {})
        routable.addRoute("/hello/:name", POST, APPLICATION_JSON.getMimeType(), post)

        when:
        // no exception
        routable.getRoute("/hello/alex", GET, APPLICATION_JSON.getMimeType())
        routable.getRoute("/hello/alex", POST, APPLICATION_JSON.getMimeType())

        var isRemoved = routable.removeRoute("/hello/:name", GET, APPLICATION_JSON.getMimeType())

        var routeFound = routable.getRoute("/hello/alex", POST, APPLICATION_JSON.getMimeType())

        // should throw an exception
        routable.getRoute("/hello/alex", GET, APPLICATION_JSON.getMimeType())

        then:
        isRemoved
        post == routeFound.route()
        var e = thrown SpottyHttpException
        e.status == NOT_FOUND
    }

}
