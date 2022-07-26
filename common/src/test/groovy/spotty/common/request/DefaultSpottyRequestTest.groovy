package spotty.common.request


import spock.lang.Specification
import spotty.common.request.params.PathParams
import spotty.common.request.params.QueryParams
import spotty.common.session.Session

import static spotty.common.http.HttpMethod.GET

class DefaultSpottyRequestTest extends Specification {

    private def request = new DefaultSpottyRequest()

    def setup() {
        request
            .protocol("HTTP/1.1")
            .scheme("http")
            .method(GET)
            .path("/hello")
            .queryParams(QueryParams.parse("key=value"))
            .pathParams(PathParams.of([key: "value"]))
            .contentLength(123)
            .contentType("application/json")
            .cookies([SSID: "some id"])
            .session(new Session().put("name", "spotty"))
            .body("hello".getBytes())
            .addHeader("header_name", "value")
            .attach("attachment")
    }

    def "should reset all fields"() {
        given:
        var emptyRequest = new DefaultSpottyRequest()

        when:
        request.reset()

        then:
        emptyRequest == request
    }
}
