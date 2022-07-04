package spotty.server

import spock.lang.*

class SpottySpec extends Specification {

    def server = new Spotty()

    def setup() {

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
