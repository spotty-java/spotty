package spotty.server.connection

import spock.lang.Specification
import spock.util.concurrent.AsyncConditions
import spotty.common.exception.SpottyStreamException
import spotty.common.request.WebRequestTestData
import spotty.common.response.ResponseWriter
import spotty.server.handler.EchoRequestHandler
import stub.SocketChannelStub

import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_WRITE

class ConnectionProcessorTest extends Specification implements WebRequestTestData {

    def "should read request correctly"() {
        given:
        var expectedRequest = aSpottyRequest()
        var socket = new SocketChannelStub(fullRequest.length())
        socket.configureBlocking(false)
        socket.write(fullRequest)
        socket.flip()

        var connection = new ConnectionProcessor(socket, new EchoRequestHandler())

        when:
        while (socket.hasRemaining()) {
            connection.handle()
        }

        then:
        connection.request == expectedRequest
    }

    def "should write response correctly"() {
        var socket = new SocketChannelStub(fullRequest.length())
        socket.configureBlocking(false)
        socket.write(fullRequest)
        socket.flip()

        var request = aSpottyRequest()
        var expectedResponse = new String(ResponseWriter.write(aSpottyResponse(request)))

        var connection = new ConnectionProcessor(socket, new EchoRequestHandler(), fullRequest.length())

        when:
        connection.handle()
        socket.clear()

        var conds = new AsyncConditions()
        conds.evaluate { connection.is(READY_TO_WRITE) }
        conds.await()

        connection.handle()
        socket.flip()

        var response = new String(socket.getAllBytes())

        then:
        response == expectedResponse
    }

    def "should throw exception when socket is blocking" () {
        given:
        var socket = new SocketChannelStub()

        when:
        new ConnectionProcessor(socket, new EchoRequestHandler())

        then:
        thrown SpottyStreamException
    }

}
