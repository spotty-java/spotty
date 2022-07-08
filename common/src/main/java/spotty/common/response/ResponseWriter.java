package spotty.common.response;

import spotty.common.exception.SpottyHttpException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import static org.apache.http.HttpHeaders.CONTENT_LENGTH;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static spotty.common.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class ResponseWriter {

    public static byte[] write(SpottyResponse response) {
        final var out = new ByteArrayOutputStream();
        final var writer = new PrintWriter(out);

        writer.println(response.getProtocol() + " " + response.getStatus().code);
        writer.println(CONTENT_LENGTH + ":" + response.getContentLength());
        writer.println(CONTENT_TYPE + ":" + response.getContentType());
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
