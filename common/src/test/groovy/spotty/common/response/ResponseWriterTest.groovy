package spotty.common.response

import spock.lang.Specification
import spotty.common.request.WebRequestTestData

class ResponseWriterTest extends Specification implements WebRequestTestData {

    def "should write response correctly"() {
        given:
        var responseWriter = new ResponseWriter()
        var content = "hello".getBytes()
        var request = aSpottyRequest()
                .contentLength(content.length)
                .body(content)

        var response = aSpottyResponse(request)

        when:
        var data = responseWriter.write(response)
        var responseString = new String(data)

        then:
        responseString == expectedResponse
    }

    def expectedResponse = """
            HTTP/1.1 200
            content-length: 5
            content-type: text/plain
            
            hello
        """.stripIndent(true).trim()

}
