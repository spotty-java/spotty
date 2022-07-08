package spotty.worker

import spock.lang.Specification
import spock.util.concurrent.AsyncConditions
import spotty.server.connection.ConnectionProcessor
import spotty.server.worker.ReactorWorker

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

class ReactorWorkerTest extends Specification {

    private final ReactorWorker reactorWorker = new ReactorWorker()

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
