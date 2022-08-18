package spotty.common.request

import spock.lang.Specification
import spotty.common.http.HttpHeaders
import spotty.common.request.params.PathParams
import spotty.common.request.params.QueryParams
import spotty.common.session.Session

import static spotty.common.http.HttpMethod.GET

class SpottyDefaultRequestTest extends Specification {

    private def request = new SpottyDefaultRequest()

    private def protocol = "HTTP/1.1"
    private def scheme = "http"
    private def method = GET
    private def path = "/hello"
    private def queryParams = QueryParams.parse("key=value")
    private def pathParams = PathParams.of([key: "value"])
    private def contentLength = 123
    private def contentType = "application/json"
    private def cookies = [SSID: "some id"]
    private def session = new Session().put("name", "spotty")
    private def body = "hello".getBytes()
    private def headers = new HttpHeaders().add("header_name", "value")
    private def host = "localhost"
    private def ip = "0.0.0.0"
    private def port = 3000
    private def attachment = new Object()

    def setup() {
        request
            .protocol(protocol)
            .scheme(scheme)
            .method(method)
            .path(path)
            .queryParams(queryParams)
            .pathParams(pathParams)
            .contentLength(contentLength)
            .contentType(contentType)
            .cookies(cookies)
            .session(session)
            .body(body)
            .addHeaders(headers)
            .host { host }
            .ip { ip }
            .port { port }
            .attach(attachment)
    }

    def "should reset all fields correctly"() {
        given:
        var emptyRequest = new SpottyDefaultRequest()

        when:
        request.reset()

        then:
        emptyRequest == request
        emptyRequest.hashCode() == request.hashCode()
    }

    def "should contain all files"() {
        when:
        var emptyRequest = new SpottyDefaultRequest()

        then:
        emptyRequest != request
        request.protocol() == protocol
        request.scheme() == scheme
        request.method() == method
        request.path() == path
        request.queryParam("key") == "value"
        request.queryParams("key") == ["value"] as Set
        request.queryParams() == queryParams.params()
        request.queryParamsObject() == queryParams
        request.queryParamsMap() == queryParams.paramsMap()
        request.pathParams() == pathParams
        request.param("key") == "value"
        request.params() == pathParams.params()
        request.contentLength() == contentLength
        request.contentType() == contentType
        request.cookies() == cookies
        request.session() == session
        request.body() == body
        request.headers() == headers
        request.host() == host
        request.ip() == ip
        request.port() == port
        request.attachment() == attachment
    }
}
