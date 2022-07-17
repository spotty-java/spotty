package spotty.server.worker

import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

import java.util.function.Consumer

class ReactorWorkerTest extends Specification {

    private final ReactorWorker reactorWorker = ReactorWorker.instance()

    def "should execute action"() {
        given:
        var message = "hello"
        var Consumer<String> consumer = Mock(Consumer.class)

        var conds = new AsyncConditions()

        when:
        reactorWorker.addAction {
            conds.evaluate { consumer.accept(message) }
        }

        conds.await()

        then:
        1 * consumer.accept(message)
    }

}
