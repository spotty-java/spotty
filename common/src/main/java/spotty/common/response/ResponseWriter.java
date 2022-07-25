package spotty.common.response;

import spotty.common.stream.output.SpottyByteArrayOutputStream;

import static spotty.common.http.HttpHeaders.CONTENT_LENGTH;
import static spotty.common.http.HttpHeaders.CONTENT_TYPE;
import static spotty.common.http.HttpHeaders.SET_COOKIE;

/**
 * Single thread use only
 */
public final class ResponseWriter {
    private static final String HEADER_SPLITTER = ": ";

    private final SpottyByteArrayOutputStream writer = new SpottyByteArrayOutputStream(2048);

    public byte[] write(SpottyResponse response) {
        try {
            writer.println(response.protocol() + " " + response.status().code);
            writer.println(CONTENT_LENGTH + HEADER_SPLITTER + response.contentLength());

            if (response.contentType() != null) {
                writer.println(CONTENT_TYPE + HEADER_SPLITTER + response.contentType());
            }

            response.headers()
                .forEach((name, value) -> {
                    writer.println(name + HEADER_SPLITTER + value);
                });

            response.cookies
                .forEach(cookie -> {
                    writer.println(SET_COOKIE + HEADER_SPLITTER + cookie);
                });

            writer.println();

            if (response.body() != null) {
                writer.write(response.body());
            }

            return writer.toByteArray();
        } finally {
            writer.reset();
        }
    }

}
