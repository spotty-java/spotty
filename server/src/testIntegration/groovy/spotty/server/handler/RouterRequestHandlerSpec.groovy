package spotty.server.handler

import org.apache.http.client.methods.HttpGet
import spotty.server.AppTestContext

import static org.apache.http.entity.ContentType.APPLICATION_JSON
import static org.apache.http.entity.ContentType.APPLICATION_XML
import static org.apache.http.entity.ContentType.WILDCARD
import static spotty.common.http.Headers.ACCEPT

class RouterRequestHandlerSpec extends AppTestContext {

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
}
