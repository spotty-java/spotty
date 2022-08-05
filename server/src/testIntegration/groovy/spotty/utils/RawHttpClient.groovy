package spotty.utils

import spotty.common.exception.SpottyException
import spotty.common.exception.SpottyHttpException
import spotty.common.http.HttpHeaders
import spotty.common.stream.output.SpottyByteArrayOutputStream
import spotty.common.stream.output.SpottyFixedByteOutputStream

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

import static spotty.common.http.HttpHeaders.CONTENT_LENGTH
import static spotty.common.http.HttpStatus.BAD_REQUEST
import static spotty.utils.RawHttpClient.State.DONE
import static spotty.utils.RawHttpClient.State.READ_BODY
import static spotty.utils.RawHttpClient.State.READ_HEADERS
import static spotty.utils.RawHttpClient.State.READ_HEAD_LINE

class RawHttpClient implements Closeable {
    private var readBuffer = ByteBuffer.allocate(1024)
    private SocketChannel channel

    RawHttpClient(int port) {
        this.channel = SocketChannel.open(new InetSocketAddress("localhost", port))
        channel.configureBlocking(false)
    }

    void get(String path) {
        query("GET", path, "")
    }

    void post(String path, String body = "") {
        query("POST", path, body)
    }

    void post(String path, byte[] body) {
        query("POST", path, body)
    }

    void query(String method, String path, String body) {
        query(method, path, body.bytes)
    }

    void query(String method, String path, byte[] body) {
        var request = new SpottyByteArrayOutputStream()
        request.println("$method $path HTTP/1.1")
        request.println("$CONTENT_LENGTH: ${body.length}")
        request.println()

        request.write(body)

        final var buffer = ByteBuffer.wrap(request.toByteArray())
        while (buffer.hasRemaining()) {
            channel.write(buffer)
        }
    }

    Response response(int timeout = 5000) {
        var state = READ_HEAD_LINE
        final var response = new Response()
        final var line = new SpottyByteArrayOutputStream(1024)
        final var body = new SpottyFixedByteOutputStream(1024)

        final var start = System.currentTimeMillis()
        while (state != DONE) {
            if (isTimeout(start, timeout)) {
                throw new SpottyException("response read timeout")
            }

            final var read = channel.read(readBuffer)
            if (read == -1 && readBuffer.position() == 0) {
                return response
            }

            if (readBuffer.position() == 0) {
                continue
            }

            readBuffer.flip()

            switch (state) {
                case READ_HEAD_LINE:
                    if (readHeadLine(readBuffer, line, response)) {
                        state = READ_HEADERS
                    }
                    break
                case READ_HEADERS:
                    if (readHeaders(readBuffer, line, response)) {
                        state = READ_BODY
                    }
                    break
                case READ_BODY:
                    if (readBody(readBuffer, body, response)) {
                        state = DONE
                    }
                    break
            }

            readBuffer.compact()
        }

        return response
    }

    @Override
    void close() {
        try {
            channel.close()
        } catch (Exception ignored) {
            // ignore
        }
    }

    private static boolean readHeadLine(ByteBuffer buffer, SpottyByteArrayOutputStream line, Response response) {
        while (buffer.hasRemaining()) {
            final byte b = buffer.get()
            if (b == '\r' as char) {
                continue
            }

            if (b == '\n' as char) {
                try {
                    response.headLine = line.toString()
                } finally {
                    line.reset()
                }

                return true
            }

            line.write(b)
        }

        return false
    }

    private static boolean readHeaders(ByteBuffer buffer, SpottyByteArrayOutputStream line, Response response) {
        while (buffer.hasRemaining()) {
            final byte b = buffer.get()
            if (b == '\r' as char) {
                continue;
            }

            if (b == '\n' as char) {
                final String header = line.toString()
                line.reset()

                if (header == "") {
                    return true
                }

                parseHeader(header, response)

                continue
            }

            line.write(b)
        }

        return false
    }

    private static void parseHeader(String line, Response response) {
        final String[] header = line.split(":", 2)
        if (header.length != 2) {
            throw new SpottyHttpException(BAD_REQUEST, "invalid header line: %s", line)
        }

        final String name = header[0].trim().toLowerCase()
        final String value = header[1].trim()

        response.headers.add(name, value)
    }

    private static boolean readBody(ByteBuffer buffer, SpottyFixedByteOutputStream body, Response response) {
        final var contentLength = response.headers.get(CONTENT_LENGTH) as int
        if (contentLength > body.capacity()) {
            body.capacity(contentLength)
        }

        if (contentLength >= 0 && body.limit() != contentLength) {
            body.limit(contentLength)
        }

        if (buffer.hasRemaining()) {
            body.writeRemaining(buffer);
        }

        if (body.isFull()) {
            response.body = body.toByteArray()
            return true
        }

        return false
    }

    private static boolean isTimeout(long start, int timeout) {
        return System.currentTimeMillis() - start > timeout
    }

    static class Response {
        String headLine
        HttpHeaders headers = new HttpHeaders()
        byte[] body

        @Override
        String toString() {
            var response = new SpottyByteArrayOutputStream()

            response.println(headLine)
            response.println(headers.toString())
            response.println()
            response.write(body)

            return response.toString()
        }
    }

    private enum State {
        READ_HEAD_LINE,
        READ_HEADERS,
        READ_BODY,
        DONE
    }
}
