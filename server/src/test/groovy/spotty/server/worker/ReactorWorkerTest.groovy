package spotty.server.worker

import spock.lang.Specification
import spotty.common.exception.SpottyException

import java.nio.ByteBuffer
import java.util.function.Consumer

class ReactorWorkerTest extends Specification {

    private final ReactorWorker reactorWorker = ReactorWorker.instance()

    def "should execute action"() {
        given:
        var buffer = Mock(ByteBuffer.class)
        var message = "hello".getBytes()

        when:
        reactorWorker.addAction { buffer.put(message, 0, 1) }.join()

        then:
        1 * buffer.put(message, 0, 1)
    }

    def "should catch exception"() {
        given:
        var ex = new SpottyException("some exception")
        var Consumer<SpottyException> handler = Mock(Consumer.class)

        when:
        reactorWorker.addAction { throw ex }
            .exceptionally { handler.accept(it.cause) }
            .join()

        then:
        1 * handler.accept(ex)
    }

}
