package spotty.common.state

import spock.lang.Specification
import spotty.common.test.TestState

import java.util.function.Consumer

import static spotty.common.test.TestState.CLOSED
import static spotty.common.test.TestState.READY_TO_READ
import static spotty.common.test.TestState.READY_TO_WRITE
import static spotty.common.test.TestState.RESPONSE_WRITE_COMPLETED

class StateMachineTest extends Specification {

    def "should change state correctly"() {
        given:
        var obj = new ClassWithState()

        Consumer<TestState> readyToRead = Mock()
        Consumer<TestState> readyToWrite = Mock()
        Consumer<TestState> responseWriteCompleted = Mock()
        Consumer<TestState> closed = Mock()

        obj.whenStateIs(READY_TO_READ, readyToRead)
        obj.whenStateIs(READY_TO_WRITE, readyToWrite)
        obj.whenStateIs(RESPONSE_WRITE_COMPLETED, responseWriteCompleted)
        obj.whenStateIs(CLOSED, closed)

        when:
        obj.changeState(READY_TO_WRITE)
        obj.changeState(RESPONSE_WRITE_COMPLETED)
        obj.changeState(READY_TO_READ)
        obj.changeState(CLOSED)

        then:
        1 * readyToRead.accept(RESPONSE_WRITE_COMPLETED)
        1 * readyToWrite.accept(READY_TO_READ)
        1 * responseWriteCompleted.accept(READY_TO_WRITE)
        1 * closed.accept(READY_TO_READ)
    }

    def "should trigger even once for duplicated change state"() {
        given:
        var obj = new ClassWithState()
        Consumer<TestState> consumer = Mock()

        obj.whenStateIs(READY_TO_WRITE, consumer)

        when:
        obj.changeState(RESPONSE_WRITE_COMPLETED)
        obj.changeState(READY_TO_WRITE)
        obj.changeState(READY_TO_WRITE)

        then:
        1 * consumer.accept(RESPONSE_WRITE_COMPLETED)
    }

    def "should trigger a few consumers for same event"() {
        given:
        var obj = new ClassWithState()
        Consumer<TestState> consumer = Mock()
        Consumer<TestState> consumer2 = Mock()

        obj.whenStateIs(READY_TO_WRITE, consumer)
        obj.whenStateIs(READY_TO_WRITE, consumer2)

        when:
        obj.changeState(RESPONSE_WRITE_COMPLETED)
        obj.changeState(READY_TO_WRITE)
        obj.changeState(READY_TO_WRITE)

        then:
        1 * consumer.accept(RESPONSE_WRITE_COMPLETED)
        1 * consumer2.accept(RESPONSE_WRITE_COMPLETED)
    }

    def "should throw an error when state is null"() {
        given:
        var obj = new ClassWithState()
        Consumer<TestState> consumer = Mock()

        obj.whenStateIs(READY_TO_WRITE, consumer)

        when:
        obj.changeState(null)

        then:
        0 * consumer.accept(_)
        thrown NullPointerException
    }

    class ClassWithState extends StateMachine<TestState> {
        ClassWithState() {
            super(READY_TO_READ)
        }
    }

}
