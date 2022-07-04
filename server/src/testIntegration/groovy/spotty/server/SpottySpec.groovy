package spotty.server

import spock.lang.Specification

class SpottySpec extends Specification {

//    private Spotty server

    def setup() {
//        server = new Spotty(3333)
    }

    def cleanup() {
//        server.awaitUntilStart()
//        server.close()
//        server.awaitUntilStop()
    }

    def "should not wait until start"() {
        given:
        var server = new Spotty()

        when:
        server.start()

        then:
        !server.isStarted()

        cleanup:
        server.awaitUntilStart()
        server.close()
        server.awaitUntilStop()
    }

    def "should wait until started"() {
        given:
        var server = new Spotty()

        when:
        server.start()
        server.awaitUntilStart()

        then:
        server.isStarted()

        cleanup:
        server.awaitUntilStart()
        server.close()
        server.awaitUntilStop()
    }

}
