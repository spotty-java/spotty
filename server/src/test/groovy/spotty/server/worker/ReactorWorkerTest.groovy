package spotty.server.worker

import spock.lang.Specification

import static org.awaitility.Awaitility.await

class ReactorWorkerTest extends Specification {

    private final ReactorWorker reactorWorker = new ReactorWorker(1)

    def "should execute action"() {
        given:
        var runIsTrue = false

        when:
        reactorWorker.addTask { runIsTrue = true }
        await().until { runIsTrue }
        reactorWorker.close()

        then:
        runIsTrue
    }

}
