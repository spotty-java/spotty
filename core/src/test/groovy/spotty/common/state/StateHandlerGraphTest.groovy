package spotty.common.state

import spock.lang.Specification
import spotty.common.exception.SpottyException
import spotty.common.state.StateHandlerGraph.Action
import spotty.common.state.StateHandlerGraph.GraphFilter
import spotty.common.test.TestState

import static spotty.common.test.TestState.CLOSED
import static spotty.common.test.TestState.DATA_REMAINING
import static spotty.common.test.TestState.READY_TO_READ
import static spotty.common.test.TestState.READY_TO_WRITE
import static spotty.common.test.TestState.RESPONSE_WRITE_COMPLETED

class StateHandlerGraphTest extends Specification {

    private def graph = new StateHandlerGraph<TestState>()

    def "should execute graph correctly"() {
        given:
        var readyToRead = Spy(action(true)) // FIXME: why does not work with Mock
        var readyToWrite = Spy(action(true))
        var responseWriteCompleted = Spy(action(true))

        graph
            .entry(READY_TO_READ).apply(readyToRead)
            .node(READY_TO_WRITE).apply(readyToWrite)
            .node(RESPONSE_WRITE_COMPLETED).apply(responseWriteCompleted)

        when:
        graph.handleState(READY_TO_WRITE)

        then:
        0 * readyToRead.execute()

        then:
        1 * readyToWrite.execute()

        then:
        1 * responseWriteCompleted.execute()
    }

    def "should stop execution when action return false in the middle of chain" () {
        given:
        var readyToRead = Spy(action(true))
        var readyToWrite = Spy(action(true))
        var responseWriteCompleted = Spy(action(false))
        var closed = Spy(action(true))

        graph
            .entry(READY_TO_READ).apply(readyToRead)
            .node(READY_TO_WRITE).apply(readyToWrite)
            .node(RESPONSE_WRITE_COMPLETED).apply(responseWriteCompleted)
            .node(CLOSED).apply(closed)

        when:
        graph.handleState(READY_TO_READ)

        then:
        1 * readyToRead.execute()

        then:
        1 * readyToWrite.execute()

        then:
        1 * responseWriteCompleted.execute()

        then:
        0 * closed.execute()
    }

    def "should not execute another chain when executing current one" () {
        given:
        var readyToRead = Spy(action(true))
        var readyToWrite = Spy(action(true))
        var responseWriteCompleted = Spy(action(false))
        var closed = Spy(action(true))

        graph
            .entry(READY_TO_READ).apply(readyToRead)
            .node(READY_TO_WRITE).apply(readyToWrite)

            .entry(RESPONSE_WRITE_COMPLETED).apply(responseWriteCompleted)
            .node(CLOSED).apply(closed)

        when:
        graph.handleState(READY_TO_READ)

        then:
        1 * readyToRead.execute()
        1 * readyToWrite.execute()
        0 * responseWriteCompleted.execute()
        0 * closed.execute()
    }

    def "should execute filter correctly"() {
        given:
        var filter = Spy(filter(true)) // FIXME: why does not work with Mock
        var readyToRead = Spy(action(true))
        var readyToWrite = Spy(action(true))
        var responseWriteCompleted = Spy(action(true))

        graph
            .filter(READY_TO_READ, READY_TO_WRITE, RESPONSE_WRITE_COMPLETED).apply(filter)
            .entry(READY_TO_READ).apply(readyToRead)
            .node(READY_TO_WRITE).apply(readyToWrite)
            .node(RESPONSE_WRITE_COMPLETED).apply(responseWriteCompleted)

        when:
        graph.handleState(READY_TO_READ)

        then:
        1 * filter.before()

        then:
        1 * readyToRead.execute()

        then:
        1 * readyToWrite.execute()

        then:
        1 * responseWriteCompleted.execute()

        then:
        1 * filter.after()
    }

    def "should stop execution when filter return false"() {
        given:
        var filter = Spy(filter(false))
        var readyToRead = Spy(action(true))
        var readyToWrite = Spy(action(true))
        var responseWriteCompleted = Spy(action(true))

        graph
            .filter(READY_TO_READ, READY_TO_WRITE, RESPONSE_WRITE_COMPLETED).apply(filter)
            .entry(READY_TO_READ).apply(readyToRead)
            .node(READY_TO_WRITE).apply(readyToWrite)
            .node(RESPONSE_WRITE_COMPLETED).apply(responseWriteCompleted)

        when:
        graph.handleState(READY_TO_READ)

        then:
        1 * filter.before()
        0 * readyToRead.execute()
        0 * readyToWrite.execute()
        0 * responseWriteCompleted.execute()
        0 * filter.after()
    }

    def "should not execute filter when run node not in filter scope"() {
        given:
        var filter = Spy(filter(false))
        var readyToRead = Spy(action(true))
        var readyToWrite = Spy(action(true))
        var responseWriteCompleted = Spy(action(true))
        var closed = Spy(action(true))

        graph
            .filter(READY_TO_READ, READY_TO_WRITE, RESPONSE_WRITE_COMPLETED).apply(filter)
            .entry(READY_TO_READ).apply(readyToRead)
            .node(READY_TO_WRITE).apply(readyToWrite)
            .node(RESPONSE_WRITE_COMPLETED).apply(responseWriteCompleted)
            .node(CLOSED).apply(closed)

        when:
        graph.handleState(CLOSED)

        then:
        0 * filter.before()
        0 * filter.after()
        0 * readyToRead.execute()
        0 * readyToWrite.execute()
        0 * responseWriteCompleted.execute()
        1 * closed.execute()
    }

    def "should execute graph when trigger 2 head nodes correctly"() {
        given:
        var readyToRead = Spy(action(true)) // FIXME: why does not work with Mock
        var readyToWrite = Spy(action(true))
        var responseWriteCompleted = Spy(action(true))
        var closed = Spy(action(true))

        graph
            .entry(DATA_REMAINING, READY_TO_READ).apply(readyToRead)
            .node(READY_TO_WRITE).apply(readyToWrite)
            .node(RESPONSE_WRITE_COMPLETED).apply(responseWriteCompleted)
            .node(CLOSED).apply(closed)

        when:
        graph.handleState(DATA_REMAINING)
        graph.handleState(READY_TO_READ)

        then:
        2 * readyToRead.execute()
        2 * readyToWrite.execute()
        2 * responseWriteCompleted.execute()
        2 * closed.execute()
    }

    private def action(boolean result) {
        return new Action() {
            @Override
            boolean execute() throws SpottyException {
                return result
            }
        }
    }

    private def filter(boolean beforeResult) {
        return new GraphFilter() {
            @Override
            boolean before() {
                return beforeResult
            }

            @Override
            void after() {

            }
        }
    }

}
