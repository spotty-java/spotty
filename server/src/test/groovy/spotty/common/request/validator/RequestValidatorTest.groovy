package spotty.common.request.validator

import spock.lang.Specification
import spotty.common.exception.SpottyHttpException
import spotty.common.request.WebRequestTestData

import static spotty.common.http.HttpMethod.POST
import static spotty.common.http.HttpStatus.BAD_REQUEST

class RequestValidatorTest extends Specification implements WebRequestTestData {

    def "should validate request successfully"() {
        given:
        var request = aSpottyRequest()

        when:
        RequestValidator.validate(request)

        then:
        noExceptionThrown()
    }

    def "should fail when request is null"() {
        when:
        RequestValidator.validate(null)

        then:
        var e = thrown SpottyHttpException
        e.status == BAD_REQUEST
    }

    def "should fail when invalid content-length"() {
        given:
        var request = aSpottyRequest().contentLength(-1)

        when:
        RequestValidator.validate(request)

        then:
        var e = thrown SpottyHttpException
        e.status == BAD_REQUEST
    }

    def "should fail when protocol #protocol, scheme #scheme, method #method or path #path is empty"() {
        given:
        var request = aSpottyRequest()
            .protocol(protocol)
            .scheme(scheme)
            .method(method)
            .path(path)

        when:
        RequestValidator.validate(request)

        then:
        var e = thrown SpottyHttpException
        e.status == BAD_REQUEST

        where:
        protocol   | scheme | method | path
        "HTTP/1.1" | "http" | POST   | null
        "HTTP/1.1" | "http" | null   | "/"
        "HTTP/1.1" | ""     | POST   | "/"
        ""         | "http" | POST   | "/"
    }

}
