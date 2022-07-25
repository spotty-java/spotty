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
            .cookie("name", "name")
            .cookie("title", "title")

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
            set-cookie: name=name
            set-cookie: title=title

            hello
        """.stripIndent(true).trim()

}
