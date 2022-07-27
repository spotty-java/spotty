package spotty.server

import org.apache.http.client.methods.HttpGet
import spotty.common.exception.SpottyException
import spotty.common.exception.SpottyHttpException
import spotty.common.request.WebRequestTestData
import spotty.common.utils.IOUtils
import spotty.AppTestContext

import static org.apache.http.entity.ContentType.APPLICATION_JSON
import static org.apache.http.entity.ContentType.APPLICATION_XML
import static org.apache.http.entity.ContentType.WILDCARD
import static spotty.common.http.HttpHeaders.ACCEPT
import static spotty.common.http.HttpStatus.BAD_REQUEST
import static spotty.common.http.HttpStatus.INTERNAL_SERVER_ERROR
import static spotty.common.http.HttpStatus.TOO_MANY_REQUESTS

class SpottyRouterSpec extends AppTestContext implements WebRequestTestData {

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
        def message = IOUtils.toString(response.entity.content)

        then:
        response.statusLine.statusCode == expectedCode.code
        message == expectedMessage

        where:
        exception                                                           | expectedCode          | expectedMessage
        new SpottyHttpException(BAD_REQUEST)                                | BAD_REQUEST           | BAD_REQUEST.statusMessage
        new SpottyHttpException(TOO_MANY_REQUESTS, "some custom message")   | TOO_MANY_REQUESTS     | "some custom message"
        new SpottyException("my SpottyException message")                   | INTERNAL_SERVER_ERROR | INTERNAL_SERVER_ERROR.statusMessage
        new RuntimeException("my RuntimeException message")                 | INTERNAL_SERVER_ERROR | INTERNAL_SERVER_ERROR.statusMessage
        new IllegalArgumentException("my IllegalArgumentException message") | INTERNAL_SERVER_ERROR | INTERNAL_SERVER_ERROR.statusMessage
        new Exception("my Exception message")                               | INTERNAL_SERVER_ERROR | INTERNAL_SERVER_ERROR.statusMessage
    }

}
