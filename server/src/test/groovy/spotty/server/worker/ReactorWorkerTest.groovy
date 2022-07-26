package spotty.server.worker

import spock.lang.Specification

import static org.awaitility.Awaitility.await

class ReactorWorkerTest extends Specification {

    private final ReactorWorker reactorWorker = ReactorWorker.instance()

    def "should execute action"() {
        given:
        var runIsTrue = false

        when:
        reactorWorker.addAction { runIsTrue = true }

        then:
        await().until { runIsTrue }
        runIsTrue
    }

}
