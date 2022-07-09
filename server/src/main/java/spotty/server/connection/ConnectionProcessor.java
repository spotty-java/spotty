package spotty.server.connection;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import spotty.common.exception.SpottyHttpException;
import spotty.common.exception.SpottyStreamException;
import spotty.common.http.Headers;
import spotty.common.http.HttpMethod;
import spotty.common.request.SpottyRequest;
import spotty.common.response.ResponseWriter;
import spotty.common.response.SpottyResponse;
import spotty.common.state.StateMachine;
import spotty.common.stream.output.SpottyByteArrayOutputStream;
import spotty.common.stream.output.SpottyFixedByteOutputStream;
import spotty.server.connection.state.ConnectionProcessorState;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.apache.commons.lang3.Validate.notNull;
import static spotty.common.http.Headers.CONTENT_LENGTH;
import static spotty.common.http.Headers.CONTENT_TYPE;
import static spotty.common.http.HttpStatus.BAD_REQUEST;
import static spotty.server.connection.state.ConnectionProcessorState.BODY_READY;
import static spotty.server.connection.state.ConnectionProcessorState.CLOSED;
import static spotty.server.connection.state.ConnectionProcessorState.HEADERS_READY;
import static spotty.server.connection.state.ConnectionProcessorState.READING_BODY;
import static spotty.server.connection.state.ConnectionProcessorState.READING_HEADERS;
import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_HANDLE_REQUEST;
import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_READ;
import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_WRITE;
import static spotty.server.connection.state.ConnectionProcessorState.REQUEST_HANDLING;
import static spotty.server.connection.state.ConnectionProcessorState.RESPONSE_READY;
import static spotty.server.connection.state.ConnectionProcessorState.RESPONSE_WRITE_COMPLETED;
import static spotty.server.connection.state.ConnectionProcessorState.RESPONSE_WRITING;

@Slf4j
public class ConnectionProcessor extends StateMachine<ConnectionProcessorState> implements Closeable {
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final int DEFAULT_LINE_SIZE = 256;

    private final SocketChannel socketChannel;
    private final ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;

    private final Headers headers = new Headers();

    private final SpottyRequest.Builder requestBuilder = SpottyRequest.builder();
    private SpottyRequest request;
    private SpottyResponse response = new SpottyResponse();

    private final SpottyByteArrayOutputStream LINE = new SpottyByteArrayOutputStream(DEFAULT_LINE_SIZE);
    private boolean firstLine = true;

    private final SpottyFixedByteOutputStream body = new SpottyFixedByteOutputStream(DEFAULT_BUFFER_SIZE);

    public ConnectionProcessor(SocketChannel socketChannel) {
        this(socketChannel, DEFAULT_BUFFER_SIZE);
    }

    public ConnectionProcessor(SocketChannel socketChannel, int bufferSize) throws SpottyStreamException {
        super(READY_TO_READ);

        this.socketChannel = notNull(socketChannel, "socketChannel");
        if (socketChannel.isBlocking()) {
            throw new SpottyStreamException("SocketChannel must be non blocking");
        }

        this.readBuffer = ByteBuffer.allocateDirect(bufferSize);
    }

    public void read() throws IOException {
        if (isReadyToHandleRequest()) {
            log.warn("request is ready, waiting to reset");
            return;
        }

        if (socketChannel.read(readBuffer) == -1) {
            close();
            return;
        }

        prepareBufferToRead();
        readHeaders();
        readBody();
        prepareBufferToWrite();

        prepareRequest();
    }

    public void write() throws IOException {
        stateTo(RESPONSE_WRITING);

        this.socketChannel.write(writeBuffer);

        if (!writeBuffer.hasRemaining()) {
            stateTo(RESPONSE_WRITE_COMPLETED);
        }
    }

    public void prepareToWrite() {
        final var data = ResponseWriter.write(response);
        this.writeBuffer = ByteBuffer.wrap(data);

        stateTo(READY_TO_WRITE);
    }

    public void requestHandlingState() {
        stateTo(REQUEST_HANDLING);
    }

    public void readyToReadState() {
        stateTo(READY_TO_READ);
    }

    public void responseReadyState() {
        stateTo(RESPONSE_READY);
    }

    public boolean isReadyToHandleRequest() {
        return state() == READY_TO_HANDLE_REQUEST;
    }

    public boolean isWriteCompleted() {
        return state() == RESPONSE_WRITE_COMPLETED;
    }

    public SpottyRequest request() {
        if (!state().is(REQUEST_HANDLING)) {
            throw new IllegalStateException("request is not ready to handle it");
        }

        return request;
    }

    public SpottyResponse response() {
        return response;
    }

    @Override
    public void close() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            // ignore
        } finally {
            stateTo(CLOSED);
        }
    }

    public void resetBuilders() {
        firstLine = true;
        headers.clear();
        body.reset();
        body.capacity(DEFAULT_BUFFER_SIZE);
        LINE.reset();
        requestBuilder.clear();
    }

    public void resetRequest() {
        request = null;
    }

    public void resetResponse() {
        this.writeBuffer = null;
        this.response = new SpottyResponse();
    }

    public boolean isOpen() {
        return socketChannel.isOpen();
    }

    private void readHeaders() {
        if (state() != READY_TO_READ && state() != READING_HEADERS) {
            return;
        }

        stateTo(READING_HEADERS);
        while (readBuffer.hasRemaining()) {
            final var b = readBuffer.get();
            if (b == '\r') {
                continue;
            }

            if (b == '\n') {
                final var line = LINE.toString();
                LINE.reset();

                if (line.equals("")) {
                    stateTo(HEADERS_READY);
                    return;
                }

                if (firstLine) {
                    firstLine = false;
                    parseHeadLine(line);
                } else {
                    parseHeader(line);
                }

                continue;
            }

            LINE.write(b);
        }
    }

    private void readBody() {
        if (state() != HEADERS_READY && state() != READING_BODY) {
            return;
        }

        stateTo(READING_BODY);
        final var contentLength = requestBuilder.contentLength;
        if (body.capacity() < contentLength) {
            body.capacity(contentLength);
        }

        if (contentLength >= 0 && body.limit() != contentLength) {
            body.limit(contentLength);
        }

        if (readBuffer.hasRemaining()) {
            body.write(readBuffer);
        }

        if (body.isFull()) {
            stateTo(BODY_READY);
        }
    }

    private void prepareBufferToRead() {
        if (readBuffer.position() > 0) {
            readBuffer.flip();
        }
    }

    private void prepareBufferToWrite() {
        if (readBuffer.hasRemaining()) {
            readBuffer.compact();
        } else {
            readBuffer.clear();
        }
    }

    private void prepareRequest() {
        if (state() == BODY_READY) {
            request = requestBuilder
                .body(body.toByteArray())
                .headers(headers)
                .build();

            resetBuilders();

            stateTo(READY_TO_HANDLE_REQUEST);
        }
    }

    private void parseHeadLine(String line) {
        final var method = line.split(" ");
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

    private static int parseContentLength(String contentLength) {
        try {
            return Integer.parseInt(contentLength);
        } catch (NumberFormatException e) {
            throw new SpottyHttpException(BAD_REQUEST, "invalid " + CONTENT_LENGTH);
        }
    }

    private static ContentType parseContentType(String contentType) {
        try {
            return ContentType.parse(contentType);
        } catch (Exception e) {
            return null;
        }
    }

    private static HttpMethod parseHttpMethod(String method) {
        final var res = HttpMethod.resolve(method.toUpperCase());
        if (res == null) {
            throw new SpottyHttpException(BAD_REQUEST, "unsupported method " + method);
        }

        return res;
    }

}
