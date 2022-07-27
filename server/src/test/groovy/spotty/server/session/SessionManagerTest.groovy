package spotty.server.session


import spock.lang.Specification
import spotty.common.cookie.Cookie
import spotty.common.exception.SpottyValidationException
import spotty.common.request.SpottyInnerRequest
import spotty.common.response.SpottyResponse

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await
import static spotty.common.http.HttpHeaders.SPOTTY_SESSION_ID

class SessionManagerTest extends Specification {
    def "should register session when enabled"() {
        given:
        var manager = SessionManager.builder().build()
        manager.enableSession()

        var request = new SpottyInnerRequest()
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
        var manager = SessionManager.builder().build()

        var request = new SpottyInnerRequest()
        var response = new SpottyResponse()

        when:
        manager.register(request, response)

        then:
        request.session() == null
        response.cookies().isEmpty()
    }

    def "should throw error when invalid sessionId"() {
        given:
        var manager = SessionManager.builder().build()
        manager.enableSession()

        var request = new SpottyInnerRequest()
        request.cookies([(SPOTTY_SESSION_ID): "wrong id"])

        var response = new SpottyResponse()

        when:
        manager.register(request, response)

        then:
        thrown SpottyValidationException
    }

    def "should expires session correctly"() {
        given:
        var ttl = 1
        var manager = SessionManager.builder()
            .sessionCheckTickDelay(ttl, SECONDS)
            .defaultSessionCookieTtl(ttl)
            .defaultSessionTtl(ttl)
            .build()

        manager.enableSession()

        var request = new SpottyInnerRequest()
        var response = new SpottyResponse()

        when:
        manager.register(request, response)

        then:
        request.session() != null
        response.cookies().contains(
            Cookie.builder()
                .name(SPOTTY_SESSION_ID)
                .value(request.session().id.toString())
                .maxAge(ttl)
                .build()
        )

        then:
        await().until(() -> !manager.sessions.containsKey(request.session().id))
        manager.sessions.isEmpty()
    }
}
