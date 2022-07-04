package spotty.server

import spock.lang.Specification

class SpottySpec extends Specification {

    private Spotty server

    def setup() {
        server = new Spotty(3333)
    }

    def cleanup() {
        server.awaitUntilStart()
        server.close()
        server.awaitUntilStop()
    }

    def "should not wait until start"() {
        when:
        server.start()

        then:
        !server.isStarted()
    }

    def "should wait until started"() {
        when:
        server.start()
        server.awaitUntilStart()

        then:
        server.isStarted()
    }

}
