/*
 * Copyright 2022 - Alex Danilenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spotty.server.connection;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotty.common.exception.SpottyException;
import spotty.common.exception.SpottyHttpException;
import spotty.common.exception.SpottyStreamException;
import spotty.common.http.HttpProtocol;
import spotty.common.request.SpottyDefaultRequest;
import spotty.common.request.params.QueryParams;
import spotty.common.response.ResponseWriter;
import spotty.common.response.SpottyResponse;
import spotty.common.state.StateHandlerGraph;
import spotty.common.state.StateHandlerGraph.GraphFilter;
import spotty.common.state.StateMachine;
import spotty.common.stream.output.SpottyByteArrayOutputStream;
import spotty.common.stream.output.SpottyFixedByteOutputStream;
import spotty.common.utils.ExceptionalCallable;
import spotty.common.utils.ExceptionalRunnable;
import spotty.server.connection.socket.SpottySocket;
import spotty.server.connection.state.ConnectionState;
import spotty.server.handler.exception.ExceptionHandler;
import spotty.server.handler.request.RequestHandler;
import spotty.server.registry.exception.ExceptionHandlerRegistry;
import spotty.server.worker.ReactorWorker;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.util.stream.Collectors.joining;
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
import static spotty.common.utils.Memoized.lazy;
import static spotty.common.validation.Validation.notNull;
import static spotty.server.connection.state.ConnectionState.BODY_READY;
import static spotty.server.connection.state.ConnectionState.BODY_READY_TO_READ;
import static spotty.server.connection.state.ConnectionState.CLOSED;
import static spotty.server.connection.state.ConnectionState.DATA_REMAINING;
import static spotty.server.connection.state.ConnectionState.HEADERS_READY_TO_READ;
import static spotty.server.connection.state.ConnectionState.INITIALIZED;
import static spotty.server.connection.state.ConnectionState.PREPARE_HEADERS;
import static spotty.server.connection.state.ConnectionState.READING_BODY;
import static spotty.server.connection.state.ConnectionState.READING_HEADERS;
import static spotty.server.connection.state.ConnectionState.READING_REQUEST_HEAD_LINE;
import static spotty.server.connection.state.ConnectionState.READY_TO_READ;
import static spotty.server.connection.state.ConnectionState.READY_TO_WRITE;
import static spotty.server.connection.state.ConnectionState.REQUEST_HANDLING;
import static spotty.server.connection.state.ConnectionState.REQUEST_READY;
import static spotty.server.connection.state.ConnectionState.RESPONSE_WRITE_COMPLETED;
import static spotty.server.connection.state.ConnectionState.RESPONSE_WRITING;

public final class Connection extends StateMachine<ConnectionState> implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(Connection.class);
    private static final AtomicLong ID_GENERATOR = new AtomicLong();

    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private static final int DEFAULT_LINE_SIZE = 256;

    public final long id = ID_GENERATOR.incrementAndGet();

    @VisibleForTesting
    final SpottyDefaultRequest request = new SpottyDefaultRequest();

    @VisibleForTesting
    final SpottyResponse response = new SpottyResponse();

    private final ResponseWriter responseWriter = new ResponseWriter();
    private final SpottyByteArrayOutputStream line = new SpottyByteArrayOutputStream(DEFAULT_LINE_SIZE);
    private final SpottyFixedByteOutputStream body = new SpottyFixedByteOutputStream(DEFAULT_BUFFER_SIZE);

    private final StateHandlerGraph<ConnectionState> stateHandlerGraph = new StateHandlerGraph<>();

    private SpottySocket socket;
    private final ReactorWorker reactorWorker;
    private final ExceptionHandlerRegistry exceptionHandlerRegistry;
    private final int maxRequestBodySize;
    private ByteBuffer readBuffer;
    private RequestHandler requestHandler;
    private ByteBuffer writeBuffer;

    public Connection(SpottySocket socket,
                      RequestHandler requestHandler,
                      ReactorWorker reactorWorker,
                      ExceptionHandlerRegistry exceptionHandlerRegistry,
                      int maxRequestBodySize) throws SpottyStreamException {
        this(socket, requestHandler, reactorWorker, exceptionHandlerRegistry, maxRequestBodySize, DEFAULT_BUFFER_SIZE);
    }

    public Connection(SpottySocket socket,
                      RequestHandler requestHandler,
                      ReactorWorker reactorWorker,
                      ExceptionHandlerRegistry exceptionHandlerRegistry,
                      int maxRequestBodySize,
                      int bufferSize) throws SpottyStreamException {
        super(INITIALIZED);

        this.socket = notNull("socket", socket);
        this.requestHandler = notNull("requestHandler", requestHandler);
        this.reactorWorker = notNull("reactorWorker", reactorWorker);
        this.exceptionHandlerRegistry = notNull("exceptionHandlerService", exceptionHandlerRegistry);
        this.maxRequestBodySize = maxRequestBodySize;

        this.readBuffer = ByteBuffer.allocate(bufferSize);

        this.stateHandlerGraph
            .filter(
                DATA_REMAINING,
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
                            final int read = socket.read(readBuffer);
                            if (read == -1) {
                                close();
                                return false;
                            }

                            if (readBuffer.position() == 0) {
                                return false;
                            }

                            // prepare buffer to read
                            readBuffer.flip();

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
            .entry(READY_TO_READ, DATA_REMAINING).apply(this::prepareRequest)
            .node(READING_REQUEST_HEAD_LINE).apply(this::readRequestHeadLine)
            .node(HEADERS_READY_TO_READ).apply(() -> changeState(READING_HEADERS))
            .node(READING_HEADERS).apply(this::readHeaders)
            .node(PREPARE_HEADERS).apply(this::prepareHeaders)
            .node(BODY_READY_TO_READ).apply(this::bodyReadyToRead)
            .node(READING_BODY).apply(this::readBody)
            .node(BODY_READY).apply(this::finishBuildRequest)
            .node(REQUEST_READY).apply(this::requestHandling)

            .entry(READY_TO_WRITE).apply(this::readyToWrite)
            .node(RESPONSE_WRITING).apply(this::writeResponse)
            .node(RESPONSE_WRITE_COMPLETED).apply(this::responseWriteCompleted)
        ;
    }

    public SelectionKey register(Selector selector) {
        final SelectionKey key = exceptionHandler(() -> socket.register(selector, OP_CONNECT, this));
        if (key == null) {
            close();
        }

        return key;
    }

    public void markDataRemaining() {
        changeState(DATA_REMAINING);
    }

    public void markReadyToRead() {
        checkStateIsOneOf(INITIALIZED, DATA_REMAINING);

        changeState(READY_TO_READ);
    }

    public void handle() {
        do {
            exceptionHandler(
                handleState,
                afterExceptionHandler // if exception respond error to the client
            );
        } while (socket.readBufferHasRemaining() && state().isReading());
    }

    // optimization to not spawn callback objects each time
    private final ExceptionalRunnable handleState = () -> stateHandlerGraph.handleState(state());

    // optimization to not spawn callback objects each time
    private final Runnable afterExceptionHandler = () -> {
        readBuffer.clear(); // reset buffer

        // close connection to not be abused with big wrong request
        // for example request with content length bigger than max limit
        // after response spotty will be reading rest content that can be in socket
        // this will cause a lot of errors (wrong headline)
        response.headers().add(CONNECTION, CLOSE.code);

        changeState(READY_TO_WRITE);
    };

    public boolean isClosed() {
        return !socket.isOpen() || is(CLOSED);
    }

    @Override
    public void close() {
        socket.close();
        changeState(CLOSED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + "]";
    }

    /**
     * first step for request, prepare it for next actions
     *
     * @return true when state was changed, false - action is not ready
     */
    private boolean prepareRequest() {
        checkStateIsOneOf(DATA_REMAINING, READY_TO_READ);

        request.host(getSocketHost);
        request.ip(getSocketIP);
        request.port(getSocketPort);

        return changeState(READING_REQUEST_HEAD_LINE);
    }

    /**
     * read socket host is expensive operation, for optimization use supplier to calculate it as lazy pattern.
     * Store host supplier as class variable to not spawn objects each time
     */
    private final Supplier<String> getSocketHost = lazy(() -> {
        try {
            final InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteAddress();

            return remoteAddress.getHostName();
        } catch (IOException e) {
            throw new SpottyException("read socket host error", e);
        }
    });

    /**
     * read socket ip is expensive operation, for optimization use supplier to calculate it as lazy pattern.
     * Store ip supplier as class variable to not spawn objects each time
     */
    private final Supplier<String> getSocketIP = lazy(() -> {
        try {
            final InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteAddress();

            return remoteAddress.getAddress().getHostAddress();
        } catch (IOException e) {
            throw new SpottyException("read socket ip error", e);
        }
    });

    /**
     * read socket port is expensive operation, for optimization use supplier to calculate it as lazy pattern.
     * Store port supplier as class variable to not spawn objects each time
     */
    private final IntSupplier getSocketPort = lazy(() -> {
        try {
            final InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteAddress();

            return remoteAddress.getPort();
        } catch (IOException e) {
            throw new SpottyException("read socket port error", e);
        }
    });

    /**
     * read first request line, ex: POST / HTTP/1.1
     *
     * @return true when state was changed, false - action is not ready
     */
    private boolean readRequestHeadLine() {
        checkStateIs(READING_REQUEST_HEAD_LINE);

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

    /**
     * read headers and parse it
     *
     * @return true when state was changed, false - action is not ready
     */
    private boolean readHeaders() {
        checkStateIs(READING_HEADERS);

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

    /**
     * after read and parse all headers, prepare and validate it to next actions
     *
     * @return true when state was changed, false - action is not ready
     */
    private boolean prepareHeaders() {
        checkStateIs(PREPARE_HEADERS);

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

    /**
     * prepare and validate body
     *
     * @return true when state was changed, false - action is not ready
     */
    private boolean bodyReadyToRead() {
        checkStateIs(BODY_READY_TO_READ);

        if (request.contentLength() > maxRequestBodySize) {
            throw new SpottyHttpException(BAD_REQUEST, "maximum body size is %s bytes, but sent %s", maxRequestBodySize, request.contentLength());
        }

        if (request.contentLength() == 0 && readBuffer.hasRemaining()) {
            throw new SpottyHttpException(BAD_REQUEST, "invalid request, content-length is 0, but body not empty");
        }

        if (request.contentLength() > body.capacity()) {
            body.capacity(request.contentLength());
        }

        if (request.contentLength() >= 0 && body.limit() != request.contentLength()) {
            body.limit(request.contentLength());
        }

        return changeState(READING_BODY);
    }

    /**
     * reading body for fixed content-length
     *
     * @return true when state was changed, false - action is not ready
     */
    private boolean readBody() {
        checkStateIs(READING_BODY);

        if (readBuffer.hasRemaining()) {
            body.writeRemaining(readBuffer);
        }

        if (body.isFull()) {
            return changeState(BODY_READY);
        }

        return false;
    }

    private boolean finishBuildRequest() {
        checkStateIs(BODY_READY);

        request.body(body.toByteArray());

        body.capacity(DEFAULT_BUFFER_SIZE);
        body.reset();

        line.reset();

        return changeState(REQUEST_READY);
    }

    /**
     * run async request handling
     *
     * @return false - stop graph execution, because request handling asynchronously
     */
    private boolean requestHandling() {
        checkStateIs(REQUEST_READY);

        changeState(REQUEST_HANDLING);
        reactorWorker.addTask(handlerRequest);

        return false;
    }

    // optimization to not spawn callback objects each time
    private final ExceptionalRunnable actionExceptionHandler = () -> requestHandler.handle(request, response);

    // optimization to not spawn callback objects each time
    private final Runnable handlerRequest = () -> {
        exceptionHandler(actionExceptionHandler);

        changeState(READY_TO_WRITE);
    };

    /**
     * preparing response to write to the socket
     *
     * @return true when state was changed, false - action is not ready
     */
    private boolean readyToWrite() {
        checkStateIs(READY_TO_WRITE);

        final byte[] data = responseWriter.write(response);
        this.writeBuffer = ByteBuffer.wrap(data);

        return changeState(RESPONSE_WRITING);
    }

    private boolean writeResponse() throws SpottyHttpException {
        checkStateIs(RESPONSE_WRITING);

        try {
            socket.write(writeBuffer);
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
        checkStateIs(RESPONSE_WRITE_COMPLETED);

        try {
            if (response.headers().hasAndEqual(CONNECTION, CLOSE.code)) {
                close();
                return false;
            }
        } finally {
            resetResponse();
            request.reset();
        }

        changeState(READY_TO_READ);

        // has something did not process
        if (readBuffer.position() > 0 || socket.readBufferHasRemaining()) {
            handle();
        }

        return false;
    }

    private void resetResponse() {
        this.writeBuffer = null;
        this.response.reset();
    }

    private void parseHeadLine(String line) {
        LOG.debug("head line: {}", line);

        final String[] method = line.split(" ");
        if (method.length != 3 || !line.contains("/")) {
            throw new SpottyHttpException(BAD_REQUEST, "invalid request head line: %s", line);
        }

        final HttpProtocol protocol = HttpProtocol.of(method[2]);
        if (protocol == null) {
            throw new SpottyHttpException(
                BAD_REQUEST,
                "Spotty is supports %s protocols only",
                HttpProtocol.VALUES.stream().map(p -> p.code).collect(joining(", "))
            );
        }

        final String scheme = method[2].split("/")[0].toLowerCase();
        final URI uri = parseUri(method[1]);

        request
            .scheme(scheme)
            .method(parseHttpMethod(method[0]))
            .path(uri.getPath())
            .queryParams(QueryParams.parse(uri.getQuery()))
            .protocol(protocol)
        ;
    }

    private void parseHeader(String line) {
        LOG.debug("request header: {}", line);

        final String[] header = line.split(":", 2);
        if (header.length != 2) {
            throw new SpottyHttpException(BAD_REQUEST, "invalid header line: %s", line);
        }

        final String name = header[0].trim().toLowerCase();
        final String value = header[1].trim();

        request.addHeader(name, value);
    }

    private void exceptionHandler(ExceptionalRunnable runnable) {
        exceptionHandler((ExceptionalCallable<Void>) () -> {
            runnable.run();
            return null;
        });
    }

    private void exceptionHandler(ExceptionalRunnable runnable, Runnable afterExceptionHandler) {
        exceptionHandler((ExceptionalCallable<Void>) () -> {
            runnable.run();
            return null;
        }, afterExceptionHandler);
    }

    private <T> T exceptionHandler(ExceptionalCallable<T> runnable) {
        return exceptionHandler(runnable, null);
    }

    @SuppressWarnings("all")
    private <T> T exceptionHandler(ExceptionalCallable<T> runnable, Runnable afterExceptionHandler) {
        try {
            return runnable.call();
        } catch (Exception exception) {
            LOG.debug("error", exception);

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

        return null;
    }

}
