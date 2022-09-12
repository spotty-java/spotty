package spotty.server

import spotty.AppTestContext

class SpottyRedirectSpec extends AppTestContext {
    def "should redirect correctly"() {
        given:
        SPOTTY.get("/hello", { req, res -> res.redirect("/bye") })
        SPOTTY.get("/bye", { req, res -> "bye" })

        when:
        var response = httpClient.get("/hello")

        then:
        response == "bye"
    }
}
