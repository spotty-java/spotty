package spotty.server

import kong.unirest.Unirest
import spotty.AppTestContext

import java.nio.file.Paths

class SpottyHttpsSpec extends AppTestContext {

    def "should execute https request correctly"() {
        given:
        SPOTTY.enableHttps(Paths.get("src/testIntegration/resources/selfsigned.jks").toAbsolutePath().toString(), "123456", null, null)
        SPOTTY.post("/", { req, res -> req.body() })

        when:
        Unirest.config().verifySsl(false)
        var response = Unirest.post(SPOTTY.hostUrl())
            .header("Content-Type", "text/plain")
            .body("hello")
            .asString()

        then:
        response.body == "hello"
    }

}
