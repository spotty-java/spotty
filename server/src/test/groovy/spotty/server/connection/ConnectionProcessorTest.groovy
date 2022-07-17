package spotty.server.connection

import spock.lang.Specification
import spock.util.concurrent.AsyncConditions
import spotty.common.exception.SpottyHttpException
import spotty.common.exception.SpottyStreamException
import spotty.common.request.WebRequestTestData
import spotty.common.response.ResponseWriter
import spotty.common.response.SpottyResponse
import spotty.server.handler.EchoRequestHandler
import stub.SocketChannelStub

import static org.apache.http.entity.ContentType.TEXT_PLAIN
import static spotty.common.http.Headers.CONTENT_TYPE
import static spotty.common.http.Headers.HOST
import static spotty.common.http.HttpStatus.BAD_REQUEST
import static spotty.common.http.HttpStatus.TOO_MANY_REQUESTS
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
        var conds = new AsyncConditions()
        connection.whenStateIs(READY_TO_WRITE, {
            conds.evaluate {
                assert connection.is(READY_TO_WRITE)
            }
        })

        connection.handle()
        socket.clear()

        conds.await()

        connection.handle()
        socket.flip()

        var response = new String(socket.getAllBytes())

        then:
        response == expectedResponse
    }

    def "should throw exception when socket is blocking"() {
        given:
        var socket = new SocketChannelStub()

        when:
        new ConnectionProcessor(socket, new EchoRequestHandler())

        then:
        thrown SpottyStreamException
    }

    def "should respond with error when request head line is wrong"() {
        given:
        var response = new SpottyResponse()
            .status(BAD_REQUEST)
            .contentType(TEXT_PLAIN)
            .body("invalid request head line")

        var expectedResult = new String(ResponseWriter.write(response))

        var socket = new SocketChannelStub()
        socket.configureBlocking(false)
        socket.write("wrong request head line")
        socket.flip()

        var connection = new ConnectionProcessor(socket, new EchoRequestHandler())

        when:
        var conds = new AsyncConditions()
        connection.whenStateIs(READY_TO_WRITE, {
            conds.evaluate {
                assert connection.is(READY_TO_WRITE)
            }
        })

        connection.handle()
        socket.clear()

        conds.await()

        connection.handle()
        socket.flip()

        var result = new String(socket.getAllBytes())

        then:
        result == expectedResult
    }

    def "should respond error when contentLength is missing"() {
        given:
        var response = new SpottyResponse()
            .status(BAD_REQUEST)
            .contentType(TEXT_PLAIN)
            .body("content-length header is required")

        var expectedResult = new String(ResponseWriter.write(response))

        var socket = new SocketChannelStub()
        socket.configureBlocking(false)
        socket.write("POST / HTTP/1.1\n")
        socket.write("$CONTENT_TYPE: text/plain\n")
        socket.write("$HOST: localhost:4000")
        socket.flip()

        var connection = new ConnectionProcessor(socket, new EchoRequestHandler())

        when:
        var conds = new AsyncConditions()
        connection.whenStateIs(READY_TO_WRITE, {
            conds.evaluate {
                assert connection.is(READY_TO_WRITE)
            }
        })

        connection.handle()
        socket.clear()

        conds.await()

        connection.handle()
        socket.flip()

        var result = new String(socket.getAllBytes())

        then:
        result == expectedResult
    }

    def "should respond error when client handler return error"() {
        given:
        var response = new SpottyResponse()
            .status(TOO_MANY_REQUESTS)
            .contentType(TEXT_PLAIN)
            .body("some message")

        var expectedResult = new String(ResponseWriter.write(response))

        var socket = new SocketChannelStub()
        socket.configureBlocking(false)
        socket.write(fullRequest)
        socket.flip()

        var connection = new ConnectionProcessor(socket, { req, res ->
            throw new SpottyHttpException(TOO_MANY_REQUESTS, "some message")
        }, fullRequest.length())

        when:
        var conds = new AsyncConditions()
        connection.whenStateIs(READY_TO_WRITE, {
            conds.evaluate {
                assert connection.is(READY_TO_WRITE)
            }
        })

        connection.handle()
        socket.clear()

        conds.await()

        connection.handle()
        socket.flip()

        var result = new String(socket.getAllBytes())

        then:
        result == expectedResult
    }

}
