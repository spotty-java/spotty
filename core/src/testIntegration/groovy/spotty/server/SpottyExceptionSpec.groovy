package spotty.server

import spotty.AppTestContext
import spotty.common.exception.SpottyHttpException

import static spotty.common.http.HttpStatus.BAD_REQUEST
import static spotty.common.http.HttpStatus.CONFLICT
import static spotty.common.http.HttpStatus.TOO_MANY_REQUESTS

class SpottyExceptionSpec extends AppTestContext {

    def "should execute registered exception handler"() {
        given:
        SPOTTY.get("/hello", { req, res -> throw new IllegalArgumentException() })
        SPOTTY.exception(
            IllegalArgumentException.class,
            { e, req, res ->
                res.body("exception handler")
            }
        )

        when:
        var response = httpClient.get("/hello")

        then:
        response == "exception handler"
    }

    def "should execute spotty main exception handler"() {
        given:
        SPOTTY.get("/hello", { req, res -> throw new SpottyHttpException(BAD_REQUEST, "exception handler") })

        when:
        var response = httpClient.getResponse("/hello")

        then:
        response.statusLine.statusCode == BAD_REQUEST.code
        response.entity.content.text == "exception handler"
    }

    def "should execute not found exception handler"() {
        given:
        SPOTTY.notFound { e, req, res ->
            res.body("not found handler")
        }

        when:
        var response = httpClient.getResponse("/hello")

        then:
        response.entity.content.text == "not found handler"
    }

    def "should halt without body"() {
        given:
        SPOTTY.get("/hello", { req, res -> SPOTTY.halt(CONFLICT) })

        when:
        var response = httpClient.getResponse("/hello")

        then:
        response.statusLine.statusCode == CONFLICT.code
        response.entity.content.text == CONFLICT.statusMessage
    }

    def "should halt with custom body"() {
        given:
        SPOTTY.get("/hello", { req, res -> SPOTTY.halt(TOO_MANY_REQUESTS, "custom message") })

        when:
        var response = httpClient.getResponse("/hello")

        then:
        response.statusLine.statusCode == TOO_MANY_REQUESTS.code
        response.entity.content.text == "custom message"
    }

}
