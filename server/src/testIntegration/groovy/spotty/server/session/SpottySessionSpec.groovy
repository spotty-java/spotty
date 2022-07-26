package spotty.server.session

import spotty.server.AppTestContext

class SpottySessionSpec extends AppTestContext {

    def setupSpec() {
        SPOTTY.enableSession()
    }

    def "should register session"() {
        given:
        SPOTTY.get("/", {req, res -> req.session().put("name", "alex") })
        SPOTTY.get("/session-value", {req, res -> req.session().get("name")})

        when:
        httpClient.get("/")
        var response = httpClient.get("/session-value")

        then:
        response == "alex"
    }
}
