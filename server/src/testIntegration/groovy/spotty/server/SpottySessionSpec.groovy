package spotty.server

import spotty.AppTestContext
import spotty.server.session.SessionManager

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await

class SpottySessionSpec extends AppTestContext {

    def setupSpec() {
        SPOTTY.close()
        SPOTTY.awaitUntilStop()

        var sessionManager = SessionManager.builder()
            .sessionCheckTickDelay(1, SECONDS)
            .defaultSessionCookieTtl(1)
            .defaultSessionTtl(1)
            .build()

        SPOTTY = new Spotty(5050, sessionManager)

        SPOTTY.enableSession()
        SPOTTY.start()
        SPOTTY.awaitUntilStart()
    }

    def "should register session"() {
        given:
        SPOTTY.get("/", { req, res -> req.session().put("name", "spotty") })
        SPOTTY.get("/session-value", { req, res -> req.session().get("name") })

        when:
        httpClient.get("/")
        var response = httpClient.get("/session-value")

        then:
        response == "spotty"
    }

    def "should session expires correctly"() {
        given:
        SPOTTY.get("/", { req, res -> req.session().put("name", "spotty") })
        SPOTTY.get("/session-value", { req, res -> req.session().get("name") })

        when:
        httpClient.get("/")
        var response = httpClient.get("/session-value")

        await().until { httpClient.get("/session-value") == "" }
        var expiredResponse = httpClient.get("/session-value")

        then:
        response == "spotty"
        expiredResponse == ""
    }
}
