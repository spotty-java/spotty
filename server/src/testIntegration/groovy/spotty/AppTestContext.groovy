package spotty

import spock.lang.Specification
import spotty.http.HttpClient

abstract class AppTestContext extends Specification {
    protected static Spotty SPOTTY
    protected static HttpClient httpClient

    def setupSpec() {
        SPOTTY = new Spotty(5050)
        SPOTTY.start()
        SPOTTY.awaitUntilStart()

        httpClient = new HttpClient(SPOTTY.port())
    }

    def cleanupSpec() {
        httpClient.close()
        SPOTTY.stop()
        SPOTTY.awaitUntilStop()
    }

    def cleanup() {
        SPOTTY.clearRoutes()
    }

}
