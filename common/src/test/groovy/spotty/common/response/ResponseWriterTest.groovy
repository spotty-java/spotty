package spotty.common.response

import spock.lang.Specification
import spotty.common.request.WebRequestTestData

class ResponseWriterTest extends Specification implements WebRequestTestData {

    def "should write response correctly"() {
        given:
        var content = "hello".getBytes()
        var request = aSpottyRequest()
                .contentLength(content.length)
                .body(content)
                .build()

        var response = aSpottyResponse(request)

        when:
        var data = ResponseWriter.write(response)
        var responseString = new String(data)

        then:
        responseString == expectedResponse
    }

    def expectedResponse = """
            HTTP/1.1 200
            content-length: 5
            content-type: text/plain
            host: localhost:4000
            connection: keep-alive
            accept-encoding: gzip, deflate, br
            user-agent: Spotty Agent
            accept: */*
            
            hello
        """.stripIndent(true).trim()

}
