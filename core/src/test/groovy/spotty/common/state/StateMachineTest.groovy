package spotty.common.state

import spock.lang.Specification
import spotty.common.exception.SpottyValidationException
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

        var Runnable readyToRead = Mock()
        var Runnable readyToWrite = Mock()
        var Runnable responseWriteCompleted = Mock()
        var Runnable closed = Mock()

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
        1 * readyToRead.run()
        1 * readyToWrite.run()
        1 * responseWriteCompleted.run()
        1 * closed.run()
    }

    def "should trigger even once for duplicated change state"() {
        given:
        var obj = new ClassWithState()
        var Runnable subscriber = Mock()

        obj.whenStateIs(READY_TO_WRITE, subscriber)

        when:
        obj.changeState(RESPONSE_WRITE_COMPLETED)
        obj.changeState(READY_TO_WRITE)
        obj.changeState(READY_TO_WRITE)

        then:
        1 * subscriber.run()
    }

    def "should trigger a few consumers for same event"() {
        given:
        var obj = new ClassWithState()
        var Runnable subscriber = Mock()
        var Runnable subscriber2 = Mock()

        obj.whenStateIs(READY_TO_WRITE, subscriber)
        obj.whenStateIs(READY_TO_WRITE, subscriber2)

        when:
        obj.changeState(RESPONSE_WRITE_COMPLETED)
        obj.changeState(READY_TO_WRITE)
        obj.changeState(READY_TO_WRITE)

        then:
        1 * subscriber.run()
        1 * subscriber2.run()
    }

    def "should throw an error when state is null"() {
        given:
        var obj = new ClassWithState()
        var Runnable subscriber = Mock()

        obj.whenStateIs(READY_TO_WRITE, subscriber)

        when:
        obj.changeState(null)

        then:
        0 * subscriber.run()
        thrown SpottyValidationException
    }

    class ClassWithState extends StateMachine<TestState> {
        ClassWithState() {
            super(READY_TO_READ)
        }
    }

}
