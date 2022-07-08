package spotty.server.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import spotty.common.exception.SpottyHttpException;
import spotty.common.exception.SpottyStreamException;
import spotty.common.http.Headers;
import spotty.common.http.HttpMethod;
import spotty.common.request.SpottyRequest;
import spotty.common.stream.SpottyFixedByteOutputStream;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.apache.commons.lang3.Validate.notNull;
import static spotty.common.http.Headers.CONTENT_LENGTH;
import static spotty.common.http.Headers.CONTENT_TYPE;
import static spotty.common.http.HttpStatus.BAD_REQUEST;

@Slf4j
public class SpottyNonBlockingRequestProvider implements Closeable {
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final int DEFAULT_LINE_SIZE = 256;

    private final SocketChannel socketChannel;
    private final ByteBuffer buffer;

    private final Headers headers = new Headers();
    private boolean readyHeaders = false;

    private final SpottyRequest.Builder requestBuilder = SpottyRequest.builder();
    private SpottyRequest request;

    private final SpottyFixedByteOutputStream LINE = new SpottyFixedByteOutputStream(DEFAULT_LINE_SIZE);
    private boolean firstLine = true;

    private final SpottyFixedByteOutputStream body = new SpottyFixedByteOutputStream(DEFAULT_BUFFER_SIZE);

    public SpottyNonBlockingRequestProvider(SocketChannel socketChannel) {
        this(socketChannel, DEFAULT_BUFFER_SIZE);
    }

    public SpottyNonBlockingRequestProvider(SocketChannel socketChannel, int bufferSize) throws SpottyStreamException {
        this.socketChannel = notNull(socketChannel, "socketChannel");
        if (socketChannel.isBlocking()) {
            throw new SpottyStreamException("SocketChannel must be non blocking");
        }

        this.buffer = ByteBuffer.allocateDirect(bufferSize);
    }

    public void read() throws IOException {
        if (isRequestReady()) {
            log.warn("request is ready, waiting to reset");
            return;
        }

        if (socketChannel.read(buffer) == -1) {
            close();
            return;
        }

        if (buffer.position() > 0) {
            buffer.flip();
        }

        readHeaders();
        readBody();

        if (buffer.hasRemaining()) {
            buffer.compact();
        } else {
            buffer.clear();
        }

        if (readyHeaders && body.isFull()) {
            request = requestBuilder
                .body(body.toByteArray())
                .headers(headers)
                .build();

            resetBuilders();
        }
    }

    public boolean isRequestReady() {
        return request != null;
    }

    public SpottyRequest request() {
        if (!isRequestReady()) {
            throw new IllegalStateException("request is not ready");
        }

        return request;
    }

    @Override
    public void close() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public void resetBuilders() {
        readyHeaders = false;
        firstLine = true;
        headers.clear();
        body.reset();
        body.capacity(DEFAULT_BUFFER_SIZE);
        LINE.reset();
        LINE.capacity(DEFAULT_LINE_SIZE);
        requestBuilder.clear();
    }

    public void resetRequest() {
        request = null;
    }

    public boolean isOpen() {
        return socketChannel.isOpen();
    }

    private void readHeaders() {
        if (readyHeaders) {
            return;
        }

        while (buffer.hasRemaining()) {
            final var b = buffer.get();
            if (b == '\r') {
                continue;
            }

            if (b == '\n') {
                final var line = LINE.toString();
                LINE.reset();

                if (line.equals("")) {
                    readyHeaders = true;
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
        if (!readyHeaders || body.isFull()) {
            return;
        }

        final var contentLength = requestBuilder.contentLength;
        if (body.capacity() < contentLength) {
            body.capacity(contentLength);
        }

        if (contentLength >= 0 && body.limit() != contentLength) {
            body.limit(contentLength);
        }

        if (buffer.hasRemaining()) {
            body.write(buffer);
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

        if (requestBuilder.method.hasBody && CONTENT_LENGTH.equals(name)) {
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
