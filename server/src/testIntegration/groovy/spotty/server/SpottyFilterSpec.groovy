package spotty.server

import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import spotty.common.filter.Filter
import spotty.AppTestContext

import static spotty.common.http.HttpHeaders.ACCEPT
import static spotty.common.http.HttpMethod.GET
import static spotty.common.http.HttpMethod.POST
import static spotty.common.http.HttpStatus.INTERNAL_SERVER_ERROR

class SpottyFilterSpec extends AppTestContext {

    def "should execute beforeAll filters"() {
        given:
        var Filter filter1 = Mock(Filter.class)
        var Filter filter2 = Mock(Filter.class)
        SPOTTY.before(filter1)
        SPOTTY.before(filter2)

        SPOTTY.get("/", { req, res -> "/" })
        SPOTTY.get("/hello", { req, res -> "hello" })
        SPOTTY.get("/product", { req, res -> "product" })

        when:
        httpClient.get("/")
        httpClient.get("/hello")
        httpClient.get("/product")

        then:
        3 * filter1.handle(_, _)
        3 * filter2.handle(_, _)
    }

    def "should execute afterAll filters"() {
        given:
        SPOTTY.get("/", { req, res -> "/" })
        SPOTTY.get("/hello", { req, res -> "hello" })
        SPOTTY.get("/product", { req, res -> "product" })

        var Filter filter1 = Mock(Filter.class)
        var Filter filter2 = Mock(Filter.class)
        SPOTTY.after(filter1)
        SPOTTY.after(filter2)

        when:
        httpClient.get("/")
        httpClient.get("/hello")
        httpClient.get("/product")

        then:
        3 * filter1.handle(_, _)
        3 * filter2.handle(_, _)
    }

    def "should execute afterAll filters when route thrown an exception"() {
        given:
        SPOTTY.get("/hello", { req, res -> throw new Exception() })

        var Filter filter = Mock(Filter.class)
        SPOTTY.after(filter)

        when:
        var response = httpClient.getResponse("/hello")

        then:
        response.statusLine.statusCode == INTERNAL_SERVER_ERROR.code
        1 * filter.handle(_, _)
    }

    def "should execute before filter by path template"() {
        given:
        var Filter helloFilter = Mock(Filter.class)
        var Filter afterHelloFilter = Mock(Filter.class)
        var Filter nonHelloFilter = Mock(Filter.class)

        SPOTTY.before("/bye", nonHelloFilter)
        SPOTTY.before("/hello", helloFilter)
        SPOTTY.before("/hello/*", afterHelloFilter)

        SPOTTY.get("/hello", { req, res -> "hello" })
        SPOTTY.get("/hello/:name", { req, res -> "hello" })

        when:
        httpClient.get("/hello")
        httpClient.get("/hello/alex")

        then:
        0 * nonHelloFilter.handle(_, _)
        1 * helloFilter.handle(_, _)
        1 * afterHelloFilter.handle(_, _)
    }

    def "should execute filters with path group correctly"() {
        given:
        var Filter before1 = Mock(Filter.class)
        var Filter after1 = Mock(Filter.class)
        var Filter before2 = Mock(Filter.class)
        var Filter after2 = Mock(Filter.class)

        SPOTTY.path("/hello", {
            SPOTTY.before(before1)

            SPOTTY.get("", { req, res -> "" })
            SPOTTY.path("/*", {
                SPOTTY.before(before2)

                SPOTTY.get("/man", { req, res -> "" })

                SPOTTY.after(after2)
            })

            SPOTTY.after(after1)
        })

        when:
        httpClient.get("/hello")
        httpClient.get("/hello/alex/man")

        then:
        1 * before2.handle(_, _)
        1 * after2.handle(_, _)
        2 * before1.handle(_, _)
        2 * after1.handle(_, _)
    }

    def "should execute filters in registered order"() {
        given:
        var Filter before1 = Mock(Filter.class)
        var Filter before2 = Mock(Filter.class)
        var Filter after1 = Mock(Filter.class)
        var Filter after2 = Mock(Filter.class)

        SPOTTY.after("/hello", after1)
        SPOTTY.after("/hello", after2)
        SPOTTY.before("/hello", before1)
        SPOTTY.before("/hello", before2)

        SPOTTY.get("/hello", { req, res -> "" })

        when:
        httpClient.get("/hello")

        then:
        1 * before1.handle(_, _)

        then:
        1 * before2.handle(_, _)

        then:
        1 * after1.handle(_, _)

        then:
        1 * after2.handle(_, _)
    }

    def "should execute filters with http method"() {
        given:
        var Filter beforeGet = Mock(Filter.class)
        var Filter afterGet = Mock(Filter.class)
        var Filter beforePost = Mock(Filter.class)
        var Filter afterPost = Mock(Filter.class)

        SPOTTY.before("/hello", GET, beforeGet)
        SPOTTY.before("/hello", POST, beforePost)

        SPOTTY.get("/hello", { req, res -> "" })
        SPOTTY.post("/hello", { req, res -> "" })

        SPOTTY.after("/hello", GET, afterGet)
        SPOTTY.after("/hello", POST, afterPost)

        when:
        httpClient.get("/hello")
        httpClient.post("/hello")

        then:
        1 * beforeGet.handle(_, _)
        1 * afterGet.handle(_, _)
        1 * beforePost.handle(_, _)
        1 * afterPost.handle(_, _)
    }

    def "should execute filters with http method and accept type"() {
        given:
        var Filter beforeGet = Mock(Filter.class)
        var Filter afterGet = Mock(Filter.class)
        var Filter beforePost = Mock(Filter.class)
        var Filter afterPost = Mock(Filter.class)

        SPOTTY.before("/hello", GET, "application/json", beforeGet)
        SPOTTY.before("/hello", POST, "application/json", beforePost)

        SPOTTY.get("/hello", "application/json", { req, res -> "json" })
        SPOTTY.get("/hello", "application/xml", { req, res -> "xml" })
        SPOTTY.post("/hello", "application/json", { req, res -> "json" })
        SPOTTY.post("/hello", "application/xml", { req, res -> "xml" })

        SPOTTY.after("/hello", GET, "application/xml", afterGet)
        SPOTTY.after("/hello", POST, "application/xml", afterPost)

        when:
        var getJson = new HttpGet("/hello")
        getJson.addHeader(ACCEPT, "application/json")
        var getJsonRes = httpClient.get(getJson)

        var getXml = new HttpGet("/hello")
        getXml.addHeader(ACCEPT, "application/xml")
        var getXmlRes = httpClient.get(getXml)

        var postJson = new HttpPost("/hello")
        postJson.addHeader(ACCEPT, "application/json")
        var postJsonRes = httpClient.post(postJson)

        var postXml = new HttpPost("/hello")
        postXml.addHeader(ACCEPT, "application/xml")
        var postXmlRes = httpClient.post(postXml)

        then:
        getJsonRes == "json"
        getXmlRes == "xml"
        postJsonRes == "json"
        postXmlRes == "xml"

        1 * beforeGet.handle(_, _)
        1 * afterGet.handle(_, _)
        1 * beforePost.handle(_, _)
        1 * afterPost.handle(_, _)
    }

}
