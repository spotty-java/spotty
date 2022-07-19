package spotty.server.router

import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpGet
import spotty.common.exception.SpottyException
import spotty.common.exception.SpottyHttpException
import spotty.common.filter.Filter
import spotty.server.AppTestContext

import static java.nio.charset.StandardCharsets.UTF_8
import static org.apache.http.entity.ContentType.APPLICATION_JSON
import static org.apache.http.entity.ContentType.APPLICATION_XML
import static org.apache.http.entity.ContentType.WILDCARD
import static spotty.common.http.Headers.ACCEPT
import static spotty.common.http.HttpStatus.BAD_REQUEST
import static spotty.common.http.HttpStatus.INTERNAL_SERVER_ERROR
import static spotty.common.http.HttpStatus.TOO_MANY_REQUESTS

class SpottyRouterSpec extends AppTestContext {

    def cleanup() {
        SPOTTY.clearRoutes()
    }

    def "should respond with query params correctly"() {
        given:
        SPOTTY.get("/hello", { req, res ->
            "hello ${req.queryParam("name")} ${req.queryParam("last_name")}"
        })

        when:
        def response = httpClient.get("/hello?name=John&last_name=Doe#id=test")

        then:
        response == "hello John Doe"
    }

    def "should respond with path params correctly"() {
        given:
        SPOTTY.get("/hello/:name/:last_name", { req, res ->
            "hello ${req.param("name")} ${req.param("last_name")}"
        })

        when:
        def response = httpClient.get("/hello/John/Doe")

        then:
        response == "hello John Doe"
    }

    def "should respond by acceptType correctly"() {
        given:
        // any acceptType
        SPOTTY.get("/hello", { req, res -> WILDCARD })
        SPOTTY.get("/hello", APPLICATION_JSON.getMimeType(), { req, res -> APPLICATION_JSON.getMimeType() })
        SPOTTY.get("/hello", APPLICATION_XML.getMimeType(), { req, res -> APPLICATION_XML.getMimeType() })

        when:
        def response = httpClient.get("/hello")

        def getJson = new HttpGet("/hello")
        getJson.addHeader(ACCEPT, APPLICATION_JSON.getMimeType())
        def responseJson = httpClient.get(getJson)

        def getXml = new HttpGet("/hello")
        getXml.addHeader(ACCEPT, APPLICATION_XML.getMimeType())
        def responseXml = httpClient.get(getXml)

        then:
        response == WILDCARD.toString()
        responseJson == APPLICATION_JSON.getMimeType()
        responseXml == APPLICATION_XML.getMimeType()
    }

    def "should catch exception when route has thrown it"() {
        given:
        SPOTTY.get("/", { req, res -> throw exception })

        when:
        def response = httpClient.getResponse("/")
        def message = IOUtils.toString(response.entity.content, UTF_8)

        then:
        response.statusLine.statusCode == expectedCode.code
        message == expectedMessage

        where:
        exception                                                           | expectedCode          | expectedMessage
        new SpottyHttpException(BAD_REQUEST)                                | BAD_REQUEST           | BAD_REQUEST.reasonPhrase
        new SpottyHttpException(TOO_MANY_REQUESTS, "some custom message")   | TOO_MANY_REQUESTS     | "some custom message"
        new SpottyException("my SpottyException message")                   | INTERNAL_SERVER_ERROR | INTERNAL_SERVER_ERROR.reasonPhrase
        new RuntimeException("my RuntimeException message")                 | INTERNAL_SERVER_ERROR | INTERNAL_SERVER_ERROR.reasonPhrase
        new IllegalArgumentException("my IllegalArgumentException message") | INTERNAL_SERVER_ERROR | INTERNAL_SERVER_ERROR.reasonPhrase
        new Exception("my Exception message")                               | INTERNAL_SERVER_ERROR | INTERNAL_SERVER_ERROR.reasonPhrase
    }

    def "should execute beforeAll filters"() {
        given:
        var Filter filter1 = Mock(Filter.class)
        var Filter filter2 = Mock(Filter.class)
        SPOTTY.before(filter1, filter2)

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
        SPOTTY.after(filter1, filter2)

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

    def "should execute filters with path group correctly" () {
        given:
        var Filter before1 = Mock(Filter.class)
        var Filter after1 = Mock(Filter.class)
        var Filter before2 = Mock(Filter.class)
        var Filter after2 = Mock(Filter.class)

        SPOTTY.path("/hello", {
            SPOTTY.before(before1)

            SPOTTY.get("", {req, res -> ""})
            SPOTTY.path("/*", {
                SPOTTY.before(before2)

                SPOTTY.get("/man", {req, res -> ""})

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

    def "should execute filters in registered order" () {
        given:
        var Filter before1 = Mock(Filter.class)
        var Filter before2 = Mock(Filter.class)
        var Filter after1 = Mock(Filter.class)
        var Filter after2 = Mock(Filter.class)

        SPOTTY.after("/hello", after1, after2)
        SPOTTY.before("/hello", before1, before2)

        SPOTTY.get("/hello", {req , res -> ""})

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
}
