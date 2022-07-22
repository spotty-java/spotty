package spotty.utils

import static spotty.common.http.HttpHeaders.CONTENT_LENGTH

class RawHttpClient implements Closeable {
    private byte[] buffer = new byte[8192]

    private Socket socket
    private InputStream reader
    private PrintWriter writer

    RawHttpClient(int port) {
        this.socket = new Socket("localhost", port)
        this.reader = socket.getInputStream()
        this.writer = new PrintWriter(socket.getOutputStream())
    }

    void get(String path) {
        query("GET", path, "")
    }

    void post(String path, String body = "") {
        query("POST", path, body)
    }

    void query(String method, String path, String body) {
        writer.println("$method $path HTTP/1.1")
        writer.println("$CONTENT_LENGTH: ${body.length()}")
        writer.println()

        writer.print(body)

        writer.flush()
    }

    String response() {
        def read = reader.read(buffer)

        return new String(buffer, 0, read)
    }

    String body() {
        var res = response()
        return res.split("\n\n")[1]
    }

    @Override
    void close() {
        try {
            socket.close()
        } catch (Exception ignored) {
            // ignore
        }
    }
}
