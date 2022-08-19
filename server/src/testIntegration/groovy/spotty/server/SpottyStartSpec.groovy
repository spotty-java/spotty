package spotty.server

import spock.lang.Specification
import spotty.Spotty

import static spotty.utils.PortGenerator.nextPort

class SpottyStartSpec extends Specification {

    private Spotty server

    def setup() {
        server = new Spotty(nextPort())
    }

    def cleanup() {
        server.stop()
    }

    def "should not wait until started"() {
        when:
        server.start()

        then:
        !server.isStarted()
        !server.isRunning()
    }

    def "should wait until started"() {
        when:
        server.start()
        server.awaitUntilStart()

        then:
        server.isStarted()
        server.isRunning()
    }

}
