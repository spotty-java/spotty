package spotty.server.connection;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import spotty.common.exception.SpottyException;
import spotty.common.exception.SpottyHttpException;
import spotty.common.exception.SpottyStreamException;
import spotty.common.http.Headers;
import spotty.common.http.HttpStatus;
import spotty.common.request.SpottyRequest;
import spotty.common.response.ResponseWriter;
import spotty.common.response.SpottyResponse;
import spotty.common.state.StateHandlerGraph;
import spotty.common.state.StateHandlerGraph.Filter;
import spotty.common.state.StateMachine;
import spotty.common.stream.output.SpottyByteArrayOutputStream;
import spotty.common.stream.output.SpottyFixedByteOutputStream;
import spotty.server.connection.state.ConnectionProcessorState;
import spotty.server.handler.RequestHandler;
import spotty.server.worker.ReactorWorker;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.apache.commons.lang3.Validate.notNull;
import static spotty.common.http.Headers.CONTENT_LENGTH;
import static spotty.common.http.Headers.CONTENT_TYPE;
import static spotty.common.http.HttpStatus.BAD_REQUEST;
import static spotty.common.request.RequestValidator.validate;
import static spotty.common.utils.HeaderUtils.parseContentLength;
import static spotty.common.utils.HeaderUtils.parseContentType;
import static spotty.common.utils.HeaderUtils.parseHttpMethod;
import static spotty.server.connection.state.ConnectionProcessorState.BODY_READY;
import static spotty.server.connection.state.ConnectionProcessorState.BODY_READY_TO_READ;
import static spotty.server.connection.state.ConnectionProcessorState.CLOSED;
import static spotty.server.connection.state.ConnectionProcessorState.HEADERS_READY_TO_READ;
import static spotty.server.connection.state.ConnectionProcessorState.READING_BODY;
import static spotty.server.connection.state.ConnectionProcessorState.READING_HEADERS;
import static spotty.server.connection.state.ConnectionProcessorState.READING_REQUEST_HEAD_LINE;
import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_READ;
import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_WRITE;
import static spotty.server.connection.state.ConnectionProcessorState.REQUEST_HANDLING;
import static spotty.server.connection.state.ConnectionProcessorState.REQUEST_READY;
import static spotty.server.connection.state.ConnectionProcessorState.RESPONSE_WRITE_COMPLETED;
import static spotty.server.connection.state.ConnectionProcessorState.RESPONSE_WRITING;

@Slf4j
public final class ConnectionProcessor extends StateMachine<ConnectionProcessorState> implements Closeable {
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final int DEFAULT_LINE_SIZE = 256;

    private static final ReactorWorker REACTOR_WORKER = ReactorWorker.instance();

    private final Headers headers = new Headers();

    private final SpottyRequest.Builder requestBuilder = SpottyRequest.builder();

    @VisibleForTesting
    SpottyRequest request;

    @VisibleForTesting
    final SpottyResponse response = new SpottyResponse();

    private final SpottyByteArrayOutputStream line = new SpottyByteArrayOutputStream(DEFAULT_LINE_SIZE);
    private final SpottyFixedByteOutputStream body = new SpottyFixedByteOutputStream(DEFAULT_BUFFER_SIZE);

    private final StateHandlerGraph<ConnectionProcessorState> stateHandlerGraph;
    private final SocketChannel socketChannel;
    private final RequestHandler requestHandler;
    private final ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;

    public ConnectionProcessor(SocketChannel socketChannel, RequestHandler requestHandler) {
        this(socketChannel, requestHandler, DEFAULT_BUFFER_SIZE);
    }

    public ConnectionProcessor(SocketChannel socketChannel, RequestHandler requestHandler, int bufferSize) throws SpottyStreamException {
        super(READY_TO_READ);

        this.socketChannel = notNull(socketChannel, "socketChannel");
        this.requestHandler = notNull(requestHandler, "requestHandler");
        if (socketChannel.isBlocking()) {
            throw new SpottyStreamException("SocketChannel must be non blocking");
        }

        this.readBuffer = ByteBuffer.allocateDirect(bufferSize);

        this.stateHandlerGraph = new StateHandlerGraph<>();
        this.stateHandlerGraph
            .filter(
                READY_TO_READ,
                READING_REQUEST_HEAD_LINE,
                HEADERS_READY_TO_READ,
                READING_HEADERS,
                BODY_READY_TO_READ,
                READING_BODY
            )
            .apply(
                new Filter() {
                    @Override
                    public boolean before() {
                        try {
                            if (socketChannel.read(readBuffer) == -1) {
                                close();
                                return false;
                            }

                            // prepare buffer to read
                            if (readBuffer.position() > 0) {
                                readBuffer.flip();
                            }

                            return true;
                        } catch (IOException e) {
                            throw new SpottyException(e);
                        }
                    }

                    @Override
                    public void after() {
                        // prepare buffer to write
                        if (readBuffer.hasRemaining()) {
                            readBuffer.compact();
                        } else {
                            readBuffer.clear();
                        }
                    }
                }
            )
            .entry(READY_TO_READ).apply(() -> stateTo(READING_REQUEST_HEAD_LINE))
            .node(READING_REQUEST_HEAD_LINE).apply(this::readRequestHeadLine)
            .node(HEADERS_READY_TO_READ).apply(() -> stateTo(READING_HEADERS))
            .node(READING_HEADERS).apply(this::readHeaders)
            .node(BODY_READY_TO_READ).apply(() -> stateTo(READING_BODY))
            .node(READING_BODY).apply(this::readBody)
            .node(BODY_READY).apply(this::prepareRequest)
            .node(REQUEST_READY).apply(this::requestHandling)

            .entry(READY_TO_WRITE).apply(() -> stateTo(RESPONSE_WRITING))
            .node(RESPONSE_WRITING).apply(this::writeResponse)
            .node(RESPONSE_WRITE_COMPLETED).apply(this::responseWriteCompleted)
        ;
    }

    public void handle() {
        stateHandlerGraph.handleState(state());
    }

    public boolean isClosed() {
        return !socketChannel.isOpen();
    }

    @Override
    public void close() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            // ignore
        }

        changeState(CLOSED);
    }

    private boolean stateTo(ConnectionProcessorState state) {
        changeState(state);

        return true;
    }

    private boolean readRequestHeadLine() {
        while (readBuffer.hasRemaining()) {
            final var b = readBuffer.get();
            if (b == '\r') {
                continue;
            }

            if (b == '\n') {
                try {
                    parseHeadLine(line.toString());
                } finally {
                    line.reset();
                }

                changeState(HEADERS_READY_TO_READ);

                return true;
            }

            line.write(b);
        }

        return false;
    }

    private boolean readHeaders() {
        while (readBuffer.hasRemaining()) {
            final var b = readBuffer.get();
            if (b == '\r') {
                continue;
            }

            if (b == '\n') {
                final var line = this.line.toString();
                this.line.reset();

                if (line.equals("")) {
                    changeState(BODY_READY_TO_READ);
                    return true;
                }

                parseHeader(line);

                continue;
            }

            line.write(b);
        }

        return false;
    }

    private boolean readBody() {
        final var contentLength = requestBuilder.contentLength;
        if (contentLength > body.capacity()) {
            body.capacity(contentLength);
        }

        if (contentLength >= 0 && body.limit() != contentLength) {
            body.limit(contentLength);
        }

        if (readBuffer.hasRemaining()) {
            body.write(readBuffer);
        }

        if (body.isFull()) {
            changeState(BODY_READY);

            return true;
        }

        return false;
    }

    private boolean prepareRequest() {
        request = requestBuilder
            .body(body.toByteArray())
            .headers(headers)
            .build();

        resetBuilders();

        changeState(REQUEST_READY);

        return true;
    }

    private boolean requestHandling() {
        changeState(REQUEST_HANDLING);

        REACTOR_WORKER.addAction(() -> {
            validate(request);
            requestHandler.handle(request, response);

            final var data = ResponseWriter.write(response);
            this.writeBuffer = ByteBuffer.wrap(data);

            changeState(READY_TO_WRITE);
        });

        return false;
    }

    private boolean writeResponse() throws SpottyHttpException {
        try {
            socketChannel.write(writeBuffer);

            if (!writeBuffer.hasRemaining()) {
                changeState(RESPONSE_WRITE_COMPLETED);
                return true;
            }

            return false;
        } catch (IOException e) {
            throw new SpottyHttpException(HttpStatus.INTERNAL_SERVER_ERROR, "socket write error", e);
        }
    }

    private boolean responseWriteCompleted() {
        resetResponse();
        resetRequest();

        changeState(READY_TO_READ);

        return false;
    }

    private void resetBuilders() {
        headers.clear();
        requestBuilder.clear();

        body.capacity(DEFAULT_BUFFER_SIZE);
        body.reset();

        line.reset();
    }

    private void resetRequest() {
        request = null;
    }

    private void resetResponse() {
        this.writeBuffer = null;
        this.response.reset();
    }

    private void parseHeadLine(String line) {
        final var method = line.split(" ");
        if (method.length != 3) {
            throw new SpottyHttpException(BAD_REQUEST, "invalid request head line");
        }

        final var scheme = method[2].split("/")[0].toLowerCase();

        requestBuilder
            .scheme(scheme)
            .method(parseHttpMethod(method[0]))
            .path(method[1])
            .protocol(method[2])
        ;
    }

    private void parseHeader(String line) {
        final var header = line.split(":", 2);
        final var name = header[0].trim().toLowerCase();
        final var value = header[1].trim();

        if (CONTENT_LENGTH.equals(name)) {
            requestBuilder.contentLength(parseContentLength(value));
        } else if (CONTENT_TYPE.equals(name)) {
            requestBuilder.contentType(parseContentType(value));
        } else {
            headers.add(name, value);
        }
    }

}
