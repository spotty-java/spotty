package spotty.server

import spotty.common.exception.SpottyHttpException
import spotty.AppTestContext

import static spotty.common.http.HttpStatus.BAD_REQUEST

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

}
