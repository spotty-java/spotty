package spotty.common.response

import spock.lang.Specification
import spotty.common.exception.SpottyHttpException
import spotty.common.exception.SpottyValidationException

import static spotty.common.http.ConnectionValue.CLOSE
import static spotty.common.http.HttpHeaders.CONNECTION
import static spotty.common.http.HttpHeaders.LOCATION
import static spotty.common.http.HttpStatus.BAD_REQUEST
import static spotty.common.http.HttpStatus.MOVED_PERMANENTLY
import static spotty.common.http.HttpStatus.NOT_FOUND
import static spotty.common.http.HttpStatus.SWITCH_PROXY
import static spotty.common.http.HttpStatus.TEMPORARY_REDIRECT

class SpottyResponseTest extends Specification {
    def "should reset all fields correctly"() {
        given:
        def emptyResponse = new SpottyResponse()
        def response = new SpottyResponse()
            .status(NOT_FOUND)
            .contentType("application/json")
            .body("hello")
            .cookie("name", "spotty")
            .addHeader("custom", "header")

        when:
        response.reset()

        then:
        emptyResponse == response
    }

    def "should create redirect to same server by default correctly"() {
        given:
        def response = new SpottyResponse()

        when:
        response.redirect("/echo")

        then:
        var e = thrown SpottyHttpException
        e.status == MOVED_PERMANENTLY
        response.status() == MOVED_PERMANENTLY
        response.headers().hasAndEqual(LOCATION, "/echo")
    }

    def "should create redirect to same server correctly"() {
        given:
        def response = new SpottyResponse()

        when:
        response.redirect("/echo", TEMPORARY_REDIRECT)

        then:
        var e = thrown SpottyHttpException
        e.status == TEMPORARY_REDIRECT
        response.status() == TEMPORARY_REDIRECT
        response.headers().hasAndEqual(LOCATION, "/echo")
    }

    def "should create redirect to different server correctly"() {
        given:
        def response = new SpottyResponse()

        when:
        response.redirect("https://google.com", SWITCH_PROXY)

        then:
        var e = thrown SpottyHttpException
        e.status == SWITCH_PROXY
        response.status() == SWITCH_PROXY
        response.headers().hasAndEqual(LOCATION, "https://google.com")
        response.headers().hasAndEqual(CONNECTION, CLOSE.code)
    }

    def "should throw validation exception when passed not redirection status"() {
        given:
        def response = new SpottyResponse()

        when:
        response.redirect("/", BAD_REQUEST)

        then:
        thrown SpottyValidationException
    }
}
