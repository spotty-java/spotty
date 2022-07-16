package spotty.server

import spock.lang.Shared
import spock.lang.Specification

abstract class AppTestContext extends Specification {

    @Shared
    protected Spotty SPOTTY

    def setupSpec() {
        SPOTTY = new Spotty(5050)
        SPOTTY.start()
        SPOTTY.awaitUntilStart()
    }

    def cleanupSpec() {
        SPOTTY.close()
        SPOTTY.awaitUntilStop()
    }

}
