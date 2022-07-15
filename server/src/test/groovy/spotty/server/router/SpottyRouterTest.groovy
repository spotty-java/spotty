package spotty.server.router

import spock.lang.Specification
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

class SpottyRouterTest extends Specification {

    private def routable = new Routable()
    private def router = new SpottyRouter(routable)

    def "should register routers correctly"() {
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

        router.get("/hello", get)
        router.post("/hello", post)
        router.put("/hello", put)
        router.patch("/hello", patch)
        router.delete("/hello", delete)
        router.head("/hello", head)
        router.trace("/hello", trace)
        router.connect("/hello", connect)
        router.options("/hello", options)

        when:
        var getFound = routable.getRoute("/hello", GET)
        var postFound = routable.getRoute("/hello", POST)
        var putFound = routable.getRoute("/hello", PUT)
        var patchFound = routable.getRoute("/hello", PATCH)
        var deleteFound = routable.getRoute("/hello", DELETE)
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

    def "should register routers with path group correctly"() {
        given:
        var Route get = {}
        var Route post = {}

        router.path("/user", {
            router.get("/hello", get)
            router.post("/hello", post)
        })

        when:
        var getFound = routable.getRoute("/user/hello", GET)
        var postFound = routable.getRoute("/user/hello", POST)

        then:
        get == getFound.route
        post == postFound.route
    }

    def "should register routers with chain of path groups correctly"() {
        given:
        var Route get = {}
        var Route post = {}
        var Route put = {}
        var Route patch = {}
        var Route delete = {}

        router.path("/api", {
            router.put("/put", put)

            router.path("/v1", {
                router.patch("/patch", patch)

                router.path("/user", {
                    router.delete("", delete)

                    router.get("/hello", get)
                    router.post("/hello", post)
                })
            })
        })

        when:
        var getFound = routable.getRoute("/api/v1/user/hello", GET)
        var postFound = routable.getRoute("/api/v1/user/hello", POST)
        var putFound = routable.getRoute("/api/put", PUT)
        var patchFound = routable.getRoute("/api/v1/patch", PATCH)
        var deleteFound = routable.getRoute("/api/v1/user", DELETE)

        then:
        router.getPathPrefix() == ""
        get == getFound.route
        post == postFound.route
        put == putFound.route
        patch == patchFound.route
        delete == deleteFound.route
    }

}
