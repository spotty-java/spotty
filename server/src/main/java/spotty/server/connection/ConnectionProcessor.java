package spotty.server.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotty.common.annotation.VisibleForTesting;
import spotty.common.exception.SpottyHttpException;
import spotty.common.exception.SpottyStreamException;
import spotty.common.request.SpottyInnerRequest;
import spotty.common.request.params.QueryParams;
import spotty.common.response.ResponseWriter;
import spotty.common.response.SpottyResponse;
import spotty.common.state.StateHandlerGraph;
import spotty.common.state.StateHandlerGraph.GraphFilter;
import spotty.common.state.StateMachine;
import spotty.common.stream.output.SpottyByteArrayOutputStream;
import spotty.common.stream.output.SpottyFixedByteOutputStream;
import spotty.server.connection.state.ConnectionProcessorState;
import spotty.server.handler.exception.ExceptionHandler;
import spotty.server.handler.request.RequestHandler;
import spotty.server.registry.exception.ExceptionHandlerRegistry;
import spotty.server.worker.ReactorWorker;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static spotty.common.http.ConnectionValue.CLOSE;
import static spotty.common.http.HttpHeaders.CONNECTION;
import static spotty.common.http.HttpHeaders.CONTENT_LENGTH;
import static spotty.common.http.HttpHeaders.CONTENT_TYPE;
import static spotty.common.http.HttpHeaders.COOKIE;
import static spotty.common.http.HttpStatus.BAD_REQUEST;
import static spotty.common.request.validator.RequestValidator.validate;
import static spotty.common.utils.HeaderUtils.parseContentLength;
import static spotty.common.utils.HeaderUtils.parseCookies;
import static spotty.common.utils.HeaderUtils.parseHttpMethod;
import static spotty.common.utils.HeaderUtils.parseUri;
import static spotty.common.validation.Validation.notNull;
import static spotty.server.connection.state.ConnectionProcessorState.BODY_READY;
import static spotty.server.connection.state.ConnectionProcessorState.BODY_READY_TO_READ;
import static spotty.server.connection.state.ConnectionProcessorState.CLOSED;
import static spotty.server.connection.state.ConnectionProcessorState.HEADERS_READY_TO_READ;
import static spotty.server.connection.state.ConnectionProcessorState.PREPARE_HEADERS;
import static spotty.server.connection.state.ConnectionProcessorState.READING_BODY;
import static spotty.server.connection.state.ConnectionProcessorState.READING_HEADERS;
import static spotty.server.connection.state.ConnectionProcessorState.READING_REQUEST_HEAD_LINE;
import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_READ;
import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_WRITE;
import static spotty.server.connection.state.ConnectionProcessorState.REQUEST_HANDLING;
import static spotty.server.connection.state.ConnectionProcessorState.REQUEST_READY;
import static spotty.server.connection.state.ConnectionProcessorState.RESPONSE_WRITE_COMPLETED;
import static spotty.server.connection.state.ConnectionProcessorState.RESPONSE_WRITING;

public final class ConnectionProcessor extends StateMachine<ConnectionProcessorState> implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionProcessor.class);

    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private static final int DEFAULT_LINE_SIZE = 256;

    private static final ReactorWorker REACTOR_WORKER = ReactorWorker.instance();

    @VisibleForTesting
    final SpottyInnerRequest request = new SpottyInnerRequest();

    @VisibleForTesting
    final SpottyResponse response = new SpottyResponse();

    private final ResponseWriter responseWriter = new ResponseWriter();
    private final SpottyByteArrayOutputStream line = new SpottyByteArrayOutputStream(DEFAULT_LINE_SIZE);
    private final SpottyFixedByteOutputStream body = new SpottyFixedByteOutputStream(DEFAULT_BUFFER_SIZE);

    private final StateHandlerGraph<ConnectionProcessorState> stateHandlerGraph = new StateHandlerGraph<>();

    private final ExceptionHandlerRegistry exceptionHandlerRegistry;
    private final SocketChannel socketChannel;
    private ByteBuffer readBuffer;
    private RequestHandler requestHandler;
    private ByteBuffer writeBuffer;

    public ConnectionProcessor(SocketChannel socketChannel,
                               RequestHandler requestHandler,
                               ExceptionHandlerRegistry exceptionHandlerRegistry) throws SpottyStreamException {
        this(socketChannel, requestHandler, exceptionHandlerRegistry, DEFAULT_BUFFER_SIZE);
    }

    public ConnectionProcessor(SocketChannel socketChannel,
                               RequestHandler requestHandler,
                               ExceptionHandlerRegistry exceptionHandlerRegistry,
                               int bufferSize) throws SpottyStreamException {
        super(READY_TO_READ);

        this.socketChannel = notNull("socketChannel", socketChannel);
        this.requestHandler = notNull("requestHandler", requestHandler);
        this.exceptionHandlerRegistry = notNull("exceptionHandlerService", exceptionHandlerRegistry);

        if (socketChannel.isBlocking()) {
            throw new SpottyStreamException("SocketChannel must be non blocking");
        }

        this.readBuffer = ByteBuffer.allocateDirect(bufferSize);

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
                new GraphFilter() {
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
                            LOG.error("socket read error", e);
                            close();
                            return false;
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
            .entry(READY_TO_READ).apply(() -> changeState(READING_REQUEST_HEAD_LINE))
            .node(READING_REQUEST_HEAD_LINE).apply(this::readRequestHeadLine)
            .node(HEADERS_READY_TO_READ).apply(() -> changeState(READING_HEADERS))
            .node(READING_HEADERS).apply(this::readHeaders)
            .node(PREPARE_HEADERS).apply(this::prepareHeaders)
            .node(BODY_READY_TO_READ).apply(() -> changeState(READING_BODY))
            .node(READING_BODY).apply(this::readBody)
            .node(BODY_READY).apply(this::prepareRequest)
            .node(REQUEST_READY).apply(this::requestHandling)

            .entry(READY_TO_WRITE).apply(this::readyToWrite)
            .node(RESPONSE_WRITING).apply(this::writeResponse)
            .node(RESPONSE_WRITE_COMPLETED).apply(this::responseWriteCompleted)
        ;
    }

    public void handle() {
        exceptionHandler(
            handleState,
            afterExceptionHandler // if exception respond error to the client
        );
    }

    // optimization to not spawn callback objects each time
    private final ExceptionalRunnable handleState = () -> stateHandlerGraph.handleState(state());

    // optimization to not spawn callback objects each time
    private final Runnable afterExceptionHandler = () -> {
        readBuffer.clear(); // reset buffer
        changeState(READY_TO_WRITE);
    };

    public boolean isClosed() {
        return !socketChannel.isOpen() || is(CLOSED);
    }

    @Override
    public void close() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            // ignore
        } finally {
            changeState(CLOSED);
        }
    }

    private boolean readRequestHeadLine() {
        while (readBuffer.hasRemaining()) {
            final byte b = readBuffer.get();
            if (b == '\r') {
                continue;
            }

            if (b == '\n') {
                try {
                    parseHeadLine(line.toString());
                } finally {
                    line.reset();
                }

                return changeState(HEADERS_READY_TO_READ);
            }

            line.write(b);
        }

        return false;
    }

    private boolean readHeaders() {
        while (readBuffer.hasRemaining()) {
            final byte b = readBuffer.get();
            if (b == '\r') {
                continue;
            }

            if (b == '\n') {
                final String line = this.line.toString();
                this.line.reset();

                if (line.equals("")) {
                    return changeState(PREPARE_HEADERS);
                }

                parseHeader(line);

                continue;
            }

            line.write(b);
        }

        return false;
    }

    private boolean prepareHeaders() {
        if (request.method().isContentLengthRequired() && request.headers().hasNot(CONTENT_LENGTH)) {
            throw new SpottyHttpException(BAD_REQUEST, CONTENT_LENGTH + " header is required");
        }

        if (request.headers().has(CONTENT_LENGTH)) {
            request.contentLength(parseContentLength(request.headers().remove(CONTENT_LENGTH)));
        }

        if (request.headers().has(CONTENT_TYPE)) {
            request.contentType(request.headers().remove(CONTENT_TYPE));
        }

        if (request.headers().has(COOKIE)) {
            request.cookies(parseCookies(request.headers().remove(COOKIE)));
        }

        validate(request);

        return changeState(BODY_READY_TO_READ);
    }

    private boolean readBody() {
        if (request.contentLength() == 0 && readBuffer.hasRemaining()) {
            throw new SpottyHttpException(BAD_REQUEST, "invalid request, content-length is 0, but body not empty");
        }

        if (request.contentLength() > body.capacity()) {
            body.capacity(request.contentLength());
        }

        if (request.contentLength() >= 0 && body.limit() != request.contentLength()) {
            body.limit(request.contentLength());
        }

        if (readBuffer.hasRemaining()) {
            body.writeRemaining(readBuffer);
        }

        if (body.isFull()) {
            return changeState(BODY_READY);
        }

        return false;
    }

    private boolean prepareRequest() {
        request.body(body.toByteArray());
        resetBuilders();

        return changeState(REQUEST_READY);
    }

    private boolean requestHandling() {
        changeState(REQUEST_HANDLING);
        REACTOR_WORKER.addAction(handlerRequest);

        return false;
    }

    // optimization to not spawn callback objects each time
    private final ExceptionalRunnable actionExceptionHandler = () -> requestHandler.handle(request, response);

    // optimization to not spawn callback objects each time
    private final Runnable handlerRequest = () -> {
        exceptionHandler(actionExceptionHandler);

        changeState(READY_TO_WRITE);
    };

    private boolean readyToWrite() {
        final byte[] data = responseWriter.write(response);
        this.writeBuffer = ByteBuffer.wrap(data);

        return changeState(RESPONSE_WRITING);
    }

    private boolean writeResponse() throws SpottyHttpException {
        try {
            socketChannel.write(writeBuffer);
            if (!writeBuffer.hasRemaining()) {
                return changeState(RESPONSE_WRITE_COMPLETED);
            }
        } catch (IOException e) {
            LOG.error("response write error", e);
            close();
        }

        return false;
    }

    private boolean responseWriteCompleted() {
        if (response.headers().hasAndEqual(CONNECTION, CLOSE.code)) {
            close();
            return false;
        }

        resetResponse();
        request.reset();

        changeState(READY_TO_READ);

        // has something did not process
        if (readBuffer.position() > 0) {
            handle();
        }

        return false;
    }

    private void resetBuilders() {
        body.capacity(DEFAULT_BUFFER_SIZE);
        body.reset();

        line.reset();
    }

    private void resetResponse() {
        this.writeBuffer = null;
        this.response.reset();
    }

    private void parseHeadLine(String line) {
        LOG.debug(line);

        final String[] method = line.split(" ");
        if (method.length != 3 || !line.contains("/")) {
            throw new SpottyHttpException(BAD_REQUEST, "invalid request head line");
        }

        final String scheme = method[2].split("/")[0].toLowerCase();
        final URI uri = parseUri(method[1]);

        request
            .scheme(scheme)
            .method(parseHttpMethod(method[0]))
            .path(uri.getPath())
            .queryParams(QueryParams.parse(uri.getQuery()))
            .protocol(method[2])
        ;
    }

    private void parseHeader(String line) {
        LOG.debug(line);

        final String[] header = line.split(":", 2);
        if (header.length != 2) {
            throw new SpottyHttpException(BAD_REQUEST, "invalid header line: " + line);
        }

        final String name = header[0].trim().toLowerCase();
        final String value = header[1].trim();

        request.addHeader(name, value);
    }

    private void exceptionHandler(ExceptionalRunnable runnable) {
        exceptionHandler(runnable, null);
    }

    @SuppressWarnings("all")
    private void exceptionHandler(ExceptionalRunnable runnable, Runnable afterExceptionHandler) {
        try {
            runnable.run();
        } catch (Exception exception) {
            LOG.debug("", exception);

            try {
                final ExceptionHandler handler = exceptionHandlerRegistry.getHandler(exception.getClass());
                handler.handle(exception, request, response);
            } catch (Exception e) {
                LOG.debug("ExceptionHandler error", e);
            } finally {
                if (afterExceptionHandler != null) {
                    afterExceptionHandler.run();
                }
            }
        }
    }

    @FunctionalInterface
    private interface ExceptionalRunnable {
        void run() throws Exception;
    }

}
