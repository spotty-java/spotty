package spotty.common.response

import spock.lang.Specification
import spotty.common.request.WebRequestTestData
import spotty.common.stream.output.SpottyByteArrayOutputStream

class ResponseHeadersWriterTest extends Specification implements WebRequestTestData {

    def "should write response correctly"() {
        given:
        var data = new SpottyByteArrayOutputStream()
        var content = "hello".getBytes()
        var request = aSpottyRequest()
            .contentLength(content.length)
            .body(content)

        var response = aSpottyResponse(request)
            .cookie("name", "name")
            .cookie("title", "title")

        when:
        ResponseHeadersWriter.write(data, response)

        then:
        data.toString() == expectedResponse
    }

    def expectedResponse = """
            HTTP/1.1 200 OK
            content-length: 5
            content-type: text/plain
            set-cookie: name=name
            set-cookie: title=title
        """.stripIndent(true).trim() + "\n\n"

}
