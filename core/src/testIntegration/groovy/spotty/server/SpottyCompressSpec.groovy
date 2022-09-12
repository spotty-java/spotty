package spotty.server

import spotty.common.request.WebRequestTestData
import spotty.AppTestContext

import static spotty.common.http.HttpHeaders.CONTENT_ENCODING

class SpottyCompressSpec extends AppTestContext implements WebRequestTestData {

    def "should respond with gzip encoding"() {
        given:
        SPOTTY.get("/hello", { req, res ->
            res.headers().add(CONTENT_ENCODING, "gzip")
            return requestBody
        })

        when:
        var response = httpClient.getResponse("/hello")

        then:
        response.entity.contentLength < requestBody.length()
        response.entity.content.text == requestBody
    }

    def "should respond with deflate encoding"() {
        given:
        SPOTTY.get("/hello", { req, res ->
            res.headers().add(CONTENT_ENCODING, "deflate")
            return requestBody
        })

        when:
        var response = httpClient.getResponse("/hello")

        then:
        response.entity.contentLength < requestBody.length()
        response.entity.content.text == requestBody
    }

}
