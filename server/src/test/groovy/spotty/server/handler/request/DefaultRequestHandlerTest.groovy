package spotty.server.handler.request

import spock.lang.Specification
import spotty.common.filter.Filter
import spotty.common.request.SpottyInnerRequest
import spotty.common.request.WebRequestTestData
import spotty.common.response.SpottyResponse
import spotty.server.compress.Compressor
import spotty.server.router.SpottyRouter
import spotty.server.router.route.Route
import spotty.server.session.SessionManager

import static java.util.concurrent.TimeUnit.SECONDS
import static spotty.common.http.HttpHeaders.CONTENT_ENCODING
import static spotty.common.http.HttpHeaders.SPOTTY_SESSION_ID
import static spotty.common.http.HttpMethod.GET

class DefaultRequestHandlerTest extends Specification implements WebRequestTestData {

    private def sessionManager = SessionManager.builder()
        .sessionCheckTickDelay(1, SECONDS)
        .defaultSessionTtl(1)
        .defaultSessionCookieTtl(1)
        .build()

    private SpottyRouter router = new SpottyRouter()
    private DefaultRequestHandler requestHandler = new DefaultRequestHandler(router, new Compressor(), sessionManager)

    def "should handler request correctly"() {
        given:
        var route = Mock(Route.class)
        route.handle(_, _) >> "hello"

        router.get("/", route)

        var response = new SpottyResponse()
        var request = new SpottyInnerRequest().method(GET).path("/")

        when:
        requestHandler.handle(request, response)

        then:
        "hello" == response.bodyAsString()
    }

    def "should execute filters"() {
        given:
        var route = Mock(Route.class)
        route.handle(_, _) >> "hello"

        var before = Mock(Filter.class)
        var after = Mock(Filter.class)

        router.get("/", route)

        router.before(before)
        router.after(after)

        var response = new SpottyResponse()
        var request = new SpottyInnerRequest().method(GET).path("/")

        when:
        requestHandler.handle(request, response)

        then:
        "hello" == response.bodyAsString()
        1 * before.handle(_, _)
        1 * after.handle(_, _)
    }

    def "should execute compressor when CONTENT_ENCODING header is present"() {
        given:
        router.get("/", { req, res ->
            res.headers().add(CONTENT_ENCODING, "gzip")
            return requestBody
        })

        var response = new SpottyResponse()
        var request = new SpottyInnerRequest().method(GET).path("/")

        when:
        requestHandler.handle(request, response)

        then:
        requestBody.length() > response.body().length
    }

    def "should not execute compressor when route handler return body as null"() {
        given:
        router.get("/", { req, res ->
            res.headers().add(CONTENT_ENCODING, "gzip")
            return null
        })

        var response = new SpottyResponse()
        var request = new SpottyInnerRequest().method(GET).path("/")

        when:
        requestHandler.handle(request, response)

        then:
        null == response.body()
    }

    def "should register session when enabled"() {
        given:
        sessionManager.enableSession()

        router.get("/", { req, res -> req.session().put("name", "spotty") })
        router.get("/session", { req, res -> req.session().get("name") })

        when:
        var response = new SpottyResponse()
        var request = new SpottyInnerRequest().method(GET).path("/")

        requestHandler.handle(request, response)
        var sessionId = response.cookies()
            .stream()
            .filter(c -> c.name() == SPOTTY_SESSION_ID)
            .map(c -> c.value())
            .findFirst()
            .get()

        var response2 = new SpottyResponse()
        var request2 = new SpottyInnerRequest()
            .method(GET)
            .path("/session")
            .cookies([(SPOTTY_SESSION_ID): sessionId])
        requestHandler.handle(request2, response2)

        then:
        "spotty" == response2.bodyAsString()

        cleanup:
        sessionManager.disableSession()
    }

}
