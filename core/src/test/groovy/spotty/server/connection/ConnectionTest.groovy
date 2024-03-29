package spotty.server.connection

import spock.lang.Specification
import spotty.common.exception.SpottyHttpException
import spotty.common.exception.SpottyStreamException
import spotty.common.exception.SpottyValidationException
import spotty.common.request.WebRequestTestData
import spotty.common.response.ResponseHeadersWriter
import spotty.common.response.SpottyResponse
import spotty.common.stream.output.SpottyByteArrayOutputStream
import spotty.server.connection.socket.SocketFactory
import spotty.server.event.ServerEvents
import spotty.server.handler.EchoRequestHandler
import spotty.server.handler.request.RequestHandler
import spotty.server.registry.exception.ExceptionHandlerRegistry
import spotty.server.worker.ReactorWorker
import stub.SocketChannelStub

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await
import static spotty.common.http.ConnectionValue.CLOSE
import static spotty.common.http.HttpHeaders.CONNECTION
import static spotty.common.http.HttpHeaders.CONTENT_TYPE
import static spotty.common.http.HttpHeaders.HOST
import static spotty.common.http.HttpHeaders.LOCATION
import static spotty.common.http.HttpStatus.BAD_REQUEST
import static spotty.common.http.HttpStatus.INTERNAL_SERVER_ERROR
import static spotty.common.http.HttpStatus.MOVED_PERMANENTLY
import static spotty.common.http.HttpStatus.TOO_MANY_REQUESTS
import static spotty.server.connection.Connection.Builder.connection
import static spotty.server.connection.state.ConnectionState.READY_TO_WRITE

class ConnectionTest extends Specification implements WebRequestTestData {
    private def socketFactory = new SocketFactory()
    private def exceptionService = new ExceptionHandlerRegistry()
    private def reactorWorker = new ReactorWorker(1, 1, 10, SECONDS)
    private def maxBodyLimit = 10 * 1024 * 1024 // 10Mb
    private def data = new SpottyByteArrayOutputStream()

    def setup() {
        exceptionService.register(SpottyHttpException.class, (exception, request, response) -> {
            response
                .status(exception.status)
                .body(exception.getMessage())
        })

        exceptionService.register(SpottyValidationException.class, (exception, request, response) -> {
            response
                .status(BAD_REQUEST)
                .body(exception.getMessage())
        })

        exceptionService.register(Exception.class, (exception, request, response) -> {
            response
                .status(INTERNAL_SERVER_ERROR)
                .body(INTERNAL_SERVER_ERROR.statusMessage)
        })
    }

    def cleanup() {
        reactorWorker.close()
    }

    def "should read request correctly"() {
        given:
        final RequestHandler delayHandler = (req, res) -> Thread.sleep(1000)
        var expectedRequest = aSpottyRequest()
        var socket = new SocketChannelStub(fullRequest.length())
        socket.configureBlocking(false)
        socket.write(fullRequest)
        socket.flip()

        var connection = connectionBuilder()
            .socket(socketFactory.createSocket(socket))
            .requestHandler(delayHandler)
            .build()

        connection.markReadyToRead()

        when:
        connection.handle()
        connection.handle()

        then:
        connection.request == expectedRequest
    }

    def "should write response correctly"() {
        var socket = new SocketChannelStub(fullRequest.length())
        socket.configureBlocking(false)
        socket.write(fullRequest)
        socket.flip()

        var request = aSpottyRequest()
        var response = aSpottyResponse(request)

        ResponseHeadersWriter.write(data, response)
        data.write(response.body())

        var expectedResponse = data.toString()

        var connection = connectionBuilder()
            .socket(socketFactory.createSocket(socket))
            .bufferSize(fullRequest.length())
            .build()

        connection.markReadyToRead()

        when:
        connection.handle()
        socket.clear()

        await().until(() -> connection.is(READY_TO_WRITE))

        connection.handle()
        socket.flip()

        var actualResponse = new String(socket.getAllBytes())

        then:
        actualResponse == expectedResponse
    }

    def "should throw exception when socket is blocking"() {
        given:
        var socket = new SocketChannelStub()
        socket.configureBlocking(true)

        when:
        connectionBuilder()
            .socket(socketFactory.createSocket(socket))
            .build()
            .markReadyToRead()

        then:
        thrown SpottyStreamException
    }

    def "should respond with error when request head line is wrong"() {
        given:
        var socket = new SocketChannelStub()
        socket.configureBlocking(false)
        socket.write("wrong request head line\n")
        socket.flip()

        var connection = connectionBuilder()
            .socket(socketFactory.createSocket(socket))
            .build()

        connection.markReadyToRead()

        when:
        connection.handle()
        socket.clear()

        await().until(() -> connection.is(READY_TO_WRITE))

        connection.handle()
        socket.flip()

        var result = new String(socket.getAllBytes())

        then:
        result == """
                    HTTP/1.1 400 Bad Request
                    content-length: 50
                    content-type: text/plain
                    connection: close
                     
                    invalid request head line: wrong request head line
                  """.stripIndent().trim()
    }

    def "should respond error when contentLength is missing"() {
        given:
        var socket = new SocketChannelStub()
        socket.configureBlocking(false)
        socket.write("POST / HTTP/1.1\n")
        socket.write("$CONTENT_TYPE: text/plain\n")
        socket.write("$HOST: localhost:4000\n\n")
        socket.flip()

        var connection = connectionBuilder()
            .socket(socketFactory.createSocket(socket))
            .build()

        connection.markReadyToRead()

        when:
        connection.handle()
        socket.clear()

        await().until(() -> connection.is(READY_TO_WRITE))

        connection.handle()
        socket.flip()

        var result = new String(socket.getAllBytes())

        then:
        result == """
                    HTTP/1.1 400 Bad Request
                    content-length: 33
                    content-type: text/plain
                    connection: close
                     
                    content-length header is required
                  """.stripIndent().trim()
    }

    def "should respond error when client handler return error"() {
        given:
        var response = new SpottyResponse()
            .status(TOO_MANY_REQUESTS)
            .contentType("text/plain")
            .body("some message")

        ResponseHeadersWriter.write(data, response)
        data.write(response.body())

        var expectedResult = data.toString()

        var socket = new SocketChannelStub()
        socket.configureBlocking(false)
        socket.write(fullRequest)
        socket.flip()

        var connection = connectionBuilder()
            .socket(socketFactory.createSocket(socket))
            .requestHandler { req, res ->
                throw new SpottyHttpException(TOO_MANY_REQUESTS, "some message")
            }
            .bufferSize(fullRequest.length())
            .build()

        connection.markReadyToRead()

        when:
        connection.handle()
        socket.clear()

        await().until(() -> connection.is(READY_TO_WRITE))

        connection.handle()
        socket.flip()

        var result = new String(socket.getAllBytes())

        then:
        result == expectedResult
    }

    def "should close connection when redirect to different server"() {
        given:
        var response = new SpottyResponse()
            .status(MOVED_PERMANENTLY)
            .contentType("text/plain")
            .addHeader(LOCATION, "https://google.com")
            .addHeader(CONNECTION, CLOSE.code)
            .body(MOVED_PERMANENTLY.statusMessage)

        ResponseHeadersWriter.write(data, response)
        data.write(response.body())

        var expectedResult = data.toString()

        var socket = new SocketChannelStub()
        socket.configureBlocking(false)
        socket.write(fullRequest)
        socket.flip()

        var connection = connectionBuilder()
            .socket(socketFactory.createSocket(socket))
            .requestHandler { req, res -> res.redirect("https://google.com") }
            .bufferSize(fullRequest.length())
            .build()

        connection.markReadyToRead()

        when:
        connection.handle()
        socket.clear()

        await().until(() -> connection.is(READY_TO_WRITE))

        connection.handle()
        socket.flip()

        var result = new String(socket.getAllBytes())

        then:
        result == expectedResult
        !socket.isOpen()
    }

    def "should return error and close connection when content length bigger then limit"() {
        given:
        var maxRequestBodySize = 10
        var socket = new SocketChannelStub()
        socket.configureBlocking(false)
        socket.write(fullRequest)
        socket.flip()

        var connection = connectionBuilder()
            .socket(socketFactory.createSocket(socket))
            .requestHandler { req, res ->
                throw new SpottyHttpException(TOO_MANY_REQUESTS, "some message")
            }
            .maxRequestBodySize(maxRequestBodySize)
            .bufferSize(fullRequest.length())
            .build()

        connection.markReadyToRead()

        when:
        connection.handle()
        socket.clear()

        await().until(() -> connection.is(READY_TO_WRITE))

        connection.handle()
        socket.flip()

        var result = new String(socket.getAllBytes())

        then:
        !socket.isOpen()
        result == """
                    HTTP/1.1 400 Bad Request
                    content-length: 44
                    content-type: text/plain
                    connection: close
                     
                    maximum body size is $maxRequestBodySize bytes, but sent ${requestBody.length()}
                  """.stripIndent().trim()
    }

    def connectionBuilder() {
        return connection()
            .requestHandler(new EchoRequestHandler())
            .serverEvents(new ServerEvents())
            .reactorWorker(reactorWorker)
            .exceptionHandlerRegistry(exceptionService)
            .maxRequestBodySize(maxBodyLimit)
    }

}
