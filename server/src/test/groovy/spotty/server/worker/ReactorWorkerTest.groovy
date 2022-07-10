package spotty.server.worker

import spock.lang.Specification
import spock.util.concurrent.AsyncConditions
import spotty.server.worker.ReactorWorker

import java.nio.ByteBuffer

class ReactorWorkerTest extends Specification {

    private final ReactorWorker reactorWorker = ReactorWorker.instance()

    def "should execute action"() {
        given:
        var buffer = Mock(ByteBuffer.class)
        var message = "hello".getBytes()

        var conds = new AsyncConditions()
        conds.evaluate { 1 * buffer.put(message, 0, 1) }

        when:
        reactorWorker.addAction {
            buffer.put(message, 0, 1)
        }
        Thread.sleep(200) // TODO: without it does not work even with await

        then:
        conds.await()
    }

}
