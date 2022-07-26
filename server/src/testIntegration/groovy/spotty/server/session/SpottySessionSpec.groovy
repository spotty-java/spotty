package spotty.server.session

import spotty.server.AppTestContext

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await

class SpottySessionSpec extends AppTestContext {

    def setupSpec() {
        SPOTTY.sessionManager(new SessionManager(1, SECONDS))
        SPOTTY.enableSession(1, 1)
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
