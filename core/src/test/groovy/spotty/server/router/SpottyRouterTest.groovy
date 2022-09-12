package spotty.server.router

import spock.lang.Specification
import spotty.common.filter.Filter
import spotty.common.router.route.Route

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

    private def router = new SpottyRouter()

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
        var getFound = router.getRoute("/hello", GET)
        var postFound = router.getRoute("/hello", POST)
        var putFound = router.getRoute("/hello", PUT)
        var patchFound = router.getRoute("/hello", PATCH)
        var deleteFound = router.getRoute("/hello", DELETE)
        var headFound = router.getRoute("/hello", HEAD)
        var traceFound = router.getRoute("/hello", TRACE)
        var connectFound = router.getRoute("/hello", CONNECT)
        var optionsFound = router.getRoute("/hello", OPTIONS)

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

    def "should register routers with acceptType correctly" () {
        given:
        var Route get = {}
        var Route post = {}
        var Route put = {}

        router.get("/hello", get)
        router.post("/hello", "application/json", post)
        router.put("/hello", "application/xml", put)

        when:
        var getFound = router.getRoute("/hello", GET)
        var postFound = router.getRoute("/hello", POST, "application/json")
        var putFound = router.getRoute("/hello", PUT, "application/xml")

        then:
        get == getFound.route()
        post == postFound.route()
        put == putFound.route()
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
        var getFound = router.getRoute("/user/hello", GET)
        var postFound = router.getRoute("/user/hello", POST)

        then:
        get == getFound.route()
        post == postFound.route()
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
        var getFound = router.getRoute("/api/v1/user/hello", GET)
        var postFound = router.getRoute("/api/v1/user/hello", POST)
        var putFound = router.getRoute("/api/put", PUT)
        var patchFound = router.getRoute("/api/v1/patch", PATCH)
        var deleteFound = router.getRoute("/api/v1/user", DELETE)

        then:
        router.pathWithPrefix("") == ""
        get == getFound.route()
        post == postFound.route()
        put == putFound.route()
        patch == patchFound.route()
        delete == deleteFound.route()
    }

    def "should register before filters correctly" () {
        given:
        var Filter before = {}
        router.before(before)

        router.get("/hello/world", {req, res -> ""})
        router.get("/bye/world", {req, res -> ""})

        when:
        var route1 = router.getRoute("/hello/world", GET)
        var route2 = router.getRoute("/bye/world", GET)

        then:
        route1.beforeFilters() == [before] as Set
        route2.beforeFilters() == [before] as Set
    }

    def "should register after filters correctly" () {
        given:
        var Filter after = {}

        router.get("/hello/world", {req, res -> ""})
        router.get("/bye/world", {req, res -> ""})
        router.after(after)

        when:
        var route1 = router.getRoute("/hello/world", GET)
        var route2 = router.getRoute("/bye/world", GET)

        then:
        route1.afterFilters() == [after] as Set
        route2.afterFilters() == [after] as Set
    }

    def "should register before filters with pathTemplate correctly" () {
        given:
        var Filter beforeAll = {}
        var Filter beforeUser = {}
        var Filter beforeProduct = {}

        router.before("/api/*", beforeAll)
        router.before("/api/*/product/*", beforeProduct)
        router.before("/api/user/*", beforeUser)

        router.get("/api/*/product/:product_id/category/:category_id", {req, res -> ""})
        router.get("/api/user/:id", {req, res -> ""})
        router.get("/hello", {req, res -> ""})

        when:
        var route1 = router.getRoute("/api/v1/product/1/category/1", GET)
        var route2 = router.getRoute("/api/user/1", GET)
        var route3 = router.getRoute("/hello", GET)

        then:
        route1.beforeFilters() == [beforeAll, beforeProduct] as Set
        route2.beforeFilters() == [beforeAll, beforeUser] as Set
        route3.beforeFilters().isEmpty()
    }

    def "should register after filters with pathTemplate correctly" () {
        given:
        var Filter afterAll = {}
        var Filter afterUser = {}
        var Filter afterProduct = {}

        router.get("/api/*/product/:product_id/category/:category_id", {req, res -> ""})
        router.get("/api/user/:id", {req, res -> ""})
        router.get("/hello", {req, res -> ""})

        router.after("/api/*", afterAll)
        router.after("/api/*/product/*", afterProduct)
        router.after("/api/user/*", afterUser)

        when:
        var route1 = router.getRoute("/api/v1/product/1/category/1", GET)
        var route2 = router.getRoute("/api/user/1", GET)
        var route3 = router.getRoute("/hello", GET)

        then:
        route1.afterFilters() == [afterAll, afterProduct] as Set
        route2.afterFilters() == [afterAll, afterUser] as Set
        route3.afterFilters().isEmpty()
    }

    def "should register before filters with http method correctly" () {
        given:
        var Filter beforeAll = {}
        var Filter beforeGet = {}
        var Filter beforePost = {}

        router.before("/api/*", beforeAll)
        router.before("/api/hello", GET, beforeGet)
        router.before("/api/hello", POST, beforePost)

        router.get("/api/some-endpoint", {req, res -> ""})
        router.get("/api/hello", {req, res -> ""})
        router.post("/api/hello", {req, res -> ""})

        when:
        var route1 = router.getRoute("/api/some-endpoint", GET)
        var route2 = router.getRoute("/api/hello", GET)
        var route3 = router.getRoute("/api/hello", POST)

        then:
        route1.beforeFilters() == [beforeAll] as Set
        route2.beforeFilters() == [beforeAll, beforeGet] as Set
        route3.beforeFilters() == [beforeAll, beforePost] as Set
    }

    def "should register after filters with http method correctly" () {
        given:
        var Filter afterAll = {}
        var Filter afterGet = {}
        var Filter afterPost = {}

        router.get("/api/some-endpoint", {req, res -> ""})
        router.get("/api/hello", {req, res -> ""})
        router.post("/api/hello", {req, res -> ""})

        router.after("/api/*", afterAll)
        router.after("/api/hello", GET, afterGet)
        router.after("/api/hello", POST, afterPost)

        when:
        var route1 = router.getRoute("/api/some-endpoint", GET)
        var route2 = router.getRoute("/api/hello", GET)
        var route3 = router.getRoute("/api/hello", POST)

        then:
        route1.afterFilters() == [afterAll] as Set
        route2.afterFilters() == [afterAll, afterGet] as Set
        route3.afterFilters() == [afterAll, afterPost] as Set
    }

    def "should register before filters with http method and accept type correctly" () {
        given:
        var Filter beforeGetJson = {}
        var Filter beforeGetXml = {}
        var Filter beforePostJson = {}
        var Filter beforePostXml = {}

        router.before("/hello", GET,"application/json", beforeGetJson)
        router.before("/hello", GET,"application/xml", beforeGetXml)
        router.before("/hello", POST,"application/json", beforePostJson)
        router.before("/hello", POST,"application/xml", beforePostXml)

        router.get("/hello", "application/json", {req, res -> ""})
        router.get("/hello", "application/xml", {req, res -> ""})
        router.post("/hello", "application/json", {req, res -> ""})
        router.post("/hello", "application/xml", {req, res -> ""})

        when:
        var getJsonFilter = router.getRoute("/hello", GET, "application/json")
        var getXmlFilter = router.getRoute("/hello", GET, "application/xml")
        var postJsonFilter = router.getRoute("/hello", POST, "application/json")
        var postXmlFilter = router.getRoute("/hello", POST, "application/xml")

        then:
        getJsonFilter.beforeFilters() == [beforeGetJson] as Set
        getXmlFilter.beforeFilters() == [beforeGetXml] as Set
        postJsonFilter.beforeFilters() == [beforePostJson] as Set
        postXmlFilter.beforeFilters() == [beforePostXml] as Set
    }

    def "should register after filters with http method and accept type correctly" () {
        given:
        var Filter afterGetJson = {}
        var Filter afterGetXml = {}
        var Filter afterPostJson = {}
        var Filter afterPostXml = {}

        router.get("/hello", "application/json", {req, res -> ""})
        router.get("/hello", "application/xml", {req, res -> ""})
        router.post("/hello", "application/json", {req, res -> ""})
        router.post("/hello", "application/xml", {req, res -> ""})

        router.after("/hello", GET,"application/json", afterGetJson)
        router.after("/hello", GET,"application/xml", afterGetXml)
        router.after("/hello", POST,"application/json", afterPostJson)
        router.after("/hello", POST,"application/xml", afterPostXml)

        when:
        var getJsonFilter = router.getRoute("/hello", GET, "application/json")
        var getXmlFilter = router.getRoute("/hello", GET, "application/xml")
        var postJsonFilter = router.getRoute("/hello", POST, "application/json")
        var postXmlFilter = router.getRoute("/hello", POST, "application/xml")

        then:
        getJsonFilter.afterFilters() == [afterGetJson] as Set
        getXmlFilter.afterFilters() == [afterGetXml] as Set
        postJsonFilter.afterFilters() == [afterPostJson] as Set
        postXmlFilter.afterFilters() == [afterPostXml] as Set
    }

}
