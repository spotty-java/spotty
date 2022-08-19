package spotty.common.request.validator

import spock.lang.Specification
import spotty.common.exception.SpottyHttpException
import spotty.common.request.SpottyDefaultRequest
import spotty.common.request.WebRequestTestData

import static spotty.common.http.HttpMethod.POST
import static spotty.common.http.HttpProtocol.HTTP_1_0
import static spotty.common.http.HttpProtocol.HTTP_1_1
import static spotty.common.http.HttpStatus.BAD_REQUEST
import static spotty.common.validation.Validation.isNotBlank
import static spotty.common.validation.Validation.isNotNull

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
        var request = new SpottyDefaultRequest()
        if (isNotNull(protocol)) {
            request.protocol(protocol)
        }

        if (isNotBlank(scheme)) {
            request.scheme(scheme)
        }

        if (isNotNull(method)) {
            request.method(method)
        }

        if (isNotNull(path)) {
            request.path(path)
        }

        when:
        RequestValidator.validate(request)

        then:
        var e = thrown SpottyHttpException
        e.status == BAD_REQUEST

        where:
        protocol | scheme | method | path
        HTTP_1_0 | "http" | POST   | null
        HTTP_1_1 | "http" | null   | "/"
        HTTP_1_1 | ""     | POST   | "/"
        null     | "http" | POST   | "/"
    }

}
