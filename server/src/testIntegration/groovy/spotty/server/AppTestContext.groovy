package spotty.server

import spock.lang.Shared
import spock.lang.Specification
import spotty.server.http.HttpClient

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
        SPOTTY.close()
        SPOTTY.awaitUntilStop()
        httpClient.close()
    }

}
