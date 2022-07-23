package spotty.server.handler.request

import spock.lang.Specification
import spotty.common.filter.Filter
import spotty.common.request.SpottyInnerRequest
import spotty.common.request.WebRequestTestData
import spotty.common.response.SpottyResponse
import spotty.server.compress.Compressor
import spotty.server.router.SpottyRouter
import spotty.server.router.route.Route

import static spotty.common.http.HttpHeaders.CONTENT_ENCODING
import static spotty.common.http.HttpMethod.GET

class DefaultRequestHandlerTest extends Specification implements WebRequestTestData {

    private SpottyRouter router = new SpottyRouter()
    private DefaultRequestHandler requestHandler = new DefaultRequestHandler(router, new Compressor())

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
}
