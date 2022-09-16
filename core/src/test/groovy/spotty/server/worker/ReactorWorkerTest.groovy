package spotty.server.worker

import spock.lang.Specification

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await

class ReactorWorkerTest extends Specification {

    private final ReactorWorker reactorWorker = new ReactorWorker(
        1,
        10,
        300,
        SECONDS
    )

    def cleanup() {
        reactorWorker.close()
    }

    def "should execute action"() {
        given:
        var runIsTrue = false

        when:
        reactorWorker.addTask { runIsTrue = true }
        await().until { runIsTrue }

        then:
        runIsTrue
    }

    def "should increase workers"() {
        when:
        for (i in 0..<1000) {
            reactorWorker.addTask { Thread.sleep(10) }
        }

        then:
        reactorWorker.reactorPool().poolSize == 10
    }

}
