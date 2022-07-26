package spotty.server.session

import spock.lang.Specification
import spotty.common.cookie.Cookie
import spotty.common.exception.SpottyValidationException
import spotty.common.request.DefaultSpottyRequest
import spotty.common.response.SpottyResponse

import static spotty.common.http.HttpHeaders.SPOTTY_SESSION_ID

class SessionManagerTest extends Specification {
    def "should register session when enabled"() {
        given:
        var manager = new SessionManager()
        manager.enableSession()

        var request = new DefaultSpottyRequest()
        var response = new SpottyResponse()

        when:
        manager.register(request, response)

        then:
        request.session() != null
        response.cookies().contains(
            Cookie.builder()
                .name(SPOTTY_SESSION_ID)
                .value(request.session().id.toString())
                .build()
        )
    }

    def "should does not register when disabled"() {
        given:
        var manager = new SessionManager()

        var request = new DefaultSpottyRequest()
        var response = new SpottyResponse()

        when:
        manager.register(request, response)

        then:
        request.session() == null
        response.cookies().isEmpty()
    }

    def "should throw error when invalid sessionId"() {
        given:
        var manager = new SessionManager()
        manager.enableSession()

        var request = new DefaultSpottyRequest()
        request.cookies([(SPOTTY_SESSION_ID): "wrong id"])

        var response = new SpottyResponse()

        when:
        manager.register(request, response)

        then:
        thrown SpottyValidationException
    }
}
