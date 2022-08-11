package spotty

import spock.lang.Shared
import spock.lang.Specification
import spotty.http.HttpClient

import static spotty.utils.PortGenerator.nextPort

abstract class AppTestContext extends Specification {
    @Shared
    protected Spotty SPOTTY

    @Shared
    protected HttpClient httpClient

    def setupSpec() {
        SPOTTY = new Spotty(nextPort())
        SPOTTY.start()
        SPOTTY.awaitUntilStart()

        httpClient = new HttpClient(SPOTTY.host(), SPOTTY.port())
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
