package spotty.common.http

import spock.lang.Specification

import static HttpHeaders.CONTENT_LENGTH
import static HttpHeaders.SERVER
import static HttpHeaders.USER_AGENT

class HttpHeadersTest extends Specification {

    def "should add headers successfully"() {
        given:
        var headers = new HttpHeaders()

        when:
        headers.add(CONTENT_LENGTH, "123")
        headers.add(SERVER, "Spotty")

        then:
        headers.size() == 2
        headers.has(CONTENT_LENGTH)
        headers.has(SERVER)
    }

    def "should add batch headers successfully"() {
        given:
        var headers = new HttpHeaders()
        var headers2 = new HttpHeaders()

        when:
        headers.add(USER_AGENT, "Spotty")

        headers2.add(CONTENT_LENGTH, "123")
        headers2.add(SERVER, "Spotty")
        headers2.add(USER_AGENT, "Spotty Agent")

        headers.add(headers2)

        then:
        headers.size() == 3
        headers.has(USER_AGENT)
        headers.has(CONTENT_LENGTH)
        headers.has(SERVER)
        headers.get(USER_AGENT) == "Spotty Agent"
    }

    def "should compare headers clone successfully"() {
        given:
        var headers = new HttpHeaders()

        when:
        headers.add(CONTENT_LENGTH, "123")
        headers.add(SERVER, "Spotty")
        headers.add(USER_AGENT, "Spotty Agent")

        var headers2 = headers.copy()

        then:
        headers == headers2
        !headers.is(headers2)
    }

    def "should clear headers successfully"(){
        given:
        var headers = new HttpHeaders()

        when:
        headers.add(CONTENT_LENGTH, "123")
        headers.add(SERVER, "Spotty")
        headers.add(USER_AGENT, "Spotty Agent")
        headers.clear()

        then:
        headers.isEmpty()
    }

}
