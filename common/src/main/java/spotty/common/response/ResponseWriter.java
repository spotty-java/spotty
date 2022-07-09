package spotty.common.response;

import spotty.common.exception.SpottyHttpException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import static spotty.common.http.Headers.CONTENT_LENGTH;
import static spotty.common.http.Headers.CONTENT_TYPE;
import static spotty.common.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class ResponseWriter {
    private static final String HEADER_SPLITTER = ": ";

    public static byte[] write(SpottyResponse response) {
        final var out = new ByteArrayOutputStream(1024);
        final var writer = new PrintWriter(out);

        writer.println(response.getProtocol() + " " + response.getStatus().code);
        writer.println(CONTENT_LENGTH + HEADER_SPLITTER + response.getContentLength());
        writer.println(CONTENT_TYPE + HEADER_SPLITTER + response.getContentType());
        response.getHeaders().forEach((name, value) -> {
            writer.println(name + HEADER_SPLITTER + value);
        });

        writer.println();
        writer.flush();

        if (response.getBody() != null) {
            try {
                out.write(response.getBody());
            } catch (IOException e) {
                throw new SpottyHttpException(INTERNAL_SERVER_ERROR, "can't write body to response", e);
            }
        }

        return out.toByteArray();
    }

}
