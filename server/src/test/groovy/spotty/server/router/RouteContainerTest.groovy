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
import static spotty.server.router.RouteContainer.NOT_FOUND_ROUTE

class RouteContainerTest extends Specification {

    private def container = new RouteContainer()

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
        container.addRoute("/hello", POST, post)
        container.addRoute("/hello", PUT, put)
        container.addRoute("/hello", PATCH, patch)
        container.addRoute("/hello", DELETE, delete)
        container.addRoute("/hello", HEAD, head)
        container.addRoute("/hello", TRACE, trace)
        container.addRoute("/hello", CONNECT, connect)
        container.addRoute("/hello", OPTIONS, options)

        when:
        var getFound = container.getRoute("/hello", GET)
        var postFound = container.getRoute("/hello", POST)
        var putFound = container.getRoute("/hello", PUT)
        var patchFound = container.getRoute("/hello", PATCH)
        var deleteFound = container.getRoute("/hello", DELETE)
        var headFound = container.getRoute("/hello", HEAD)
        var traceFound = container.getRoute("/hello", TRACE)
        var connectFound = container.getRoute("/hello", CONNECT)
        var optionsFound = container.getRoute("/hello", OPTIONS)

        then:
        get == getFound
        post == postFound
        put == putFound
        patch == patchFound
        delete == deleteFound
        head == headFound
        trace == traceFound
        connect == connectFound
        options == optionsFound
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
        notFoundPath == NOT_FOUND_ROUTE
        notFoundMethod == NOT_FOUND_ROUTE
    }

    def "should throw an error when path is duplicated" () {
        when:
        container.addRoute("/hello", GET, {})
        container.addRoute("/hello", GET, {})

        then:
        thrown SpottyException
    }
}
