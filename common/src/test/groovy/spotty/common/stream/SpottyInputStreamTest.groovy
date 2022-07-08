package spotty.common.stream

import spock.lang.Specification
import spotty.common.request.WebRequestTestData

class SpottyInputStreamTest extends Specification implements WebRequestTestData {

    def "should read all data from steam correctly"() {
        given:
        var data = fullRequest.getBytes()
        var input = new ByteArrayInputStream(data)
        var spottyStream = new SpottyInputStream(input)
        spottyStream.fixedContentSize(data.length)

        when:
        var actual = new String(spottyStream.readAllBytes())

        then:
        actual == fullRequest
    }

    def "should read all data line by line from steam correctly"() {
        given:
        var input = new SpottyInputStream(new ByteArrayInputStream(fullRequest.getBytes()))

        when:
        var result = ""
        var line
        while ((line = input.readLine()) != null) {
            result += "$line\n"
        }

        result = result.trim()

        then:
        fullRequest == result
    }

    def "should read limited data from stream"() {
        given:
        var limit = 10
        var data = fullRequest.getBytes()
        var input = new ByteArrayInputStream(data)
        var spottyStream = new SpottyInputStream(input)
        spottyStream.fixedContentSize(limit)
        var expected = new byte[limit]
        System.arraycopy(data, 0, expected, 0, limit)

        when:
        var actual = spottyStream.readAllBytes()

        then:
        actual.length == limit
        actual == expected
    }

    def "should read limited data with offset"() {
        given:
        var limit = 10
        var offset = 5
        var len = 5
        var data = fullRequest.getBytes()
        var input = new ByteArrayInputStream(data)
        var spottyStream = new SpottyInputStream(input)
        spottyStream.fixedContentSize(limit)
        var expected = new byte[limit]
        System.arraycopy(data, 0, expected, offset, len)

        when:
        var actual = new byte[limit]
        var read = spottyStream.read(actual, offset, len)

        then:
        read == len
        actual == expected
    }

    def "should read separately correctly"() {
        given:
        var inputText = "line1\r\nline2\r\n\r\nline3"
        var expected = "line1\nline2\n\nline3"
        var spottyStream = new SpottyInputStream(new ByteArrayInputStream(inputText.getBytes()))

        when:
        var content = ""
        var line
        while ((line = spottyStream.readLine()) != null) {
            if (line == "") {
                break
            }

            content += "$line\n"
        }

        spottyStream.fixedContentSize(5)
        content = "$content\n" + new String(spottyStream.readAllBytes())

        then:
        expected == content
    }
}
