package spotty

import spock.lang.Shared
import spock.lang.Specification
import spotty.http.HttpClient

abstract class AppTestContext extends Specification {

    @Shared
    protected Spotty SPOTTY

    @Shared
    protected HttpClient httpClient

    def setupSpec() {
        SPOTTY = new Spotty(5050)
        SPOTTY.start()
        SPOTTY.awaitUntilStart()

        httpClient = new HttpClient(SPOTTY.port())
    }

    def cleanupSpec() {
        httpClient.close()
        SPOTTY.close()
        SPOTTY.awaitUntilStop()
    }

    def cleanup() {
        SPOTTY.clearRoutes()
    }

}
