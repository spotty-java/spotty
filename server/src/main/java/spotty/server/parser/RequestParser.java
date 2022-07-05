package spotty.server.parser;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import spotty.server.request.SpottyRequest;
import spotty.server.stream.SpottyInputStream;

import java.io.InputStream;
import java.util.HashMap;

import static spotty.server.stream.SpottyInputStream.EMPTY;

@Slf4j
public class RequestParser {

    @SneakyThrows
    public static SpottyRequest parse(InputStream in) {
        final var reader = new SpottyInputStream(in);
        final var methodRaw = reader.readLine();
        if (methodRaw == null) {
            return null;
        }

        final var headers = new HashMap<String, String>();
        final var method = methodRaw.split(" ");
        final var scheme = method[2].split("/")[0].toLowerCase();

        final var request = SpottyRequest.builder()
            .scheme(scheme)
            .method(method[0])
            .path(method[1])
            .protocol(method[2]);

        String line;
        while (true) {
            line = reader.readLine();
            if (line == null || line.equals("")) {
                break;
            }

            final var header = line.split(":", 2);
            headers.put(header[0].trim().toLowerCase(), header[1].trim());
        }

        final var contentLength = parseContentLength(headers.remove("content-length"));
        final var contentType = headers.remove("content-type");
        var body = EMPTY;
        if (contentLength > 0) {
            body = reader;
            reader.fixedContentSize(contentLength);
        }

        return request
            .body(body)
            .contentType(parseContentType(contentType))
            .contentLength(contentLength)
            .headers(headers)
            .build();
    }

    private static long parseContentLength(String contentLength) {
        try {
            return Long.parseLong(contentLength);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static ContentType parseContentType(String contentType) {
        try {
            return ContentType.parse(contentType);
        } catch (Exception e) {
            return null;
        }
    }

}
