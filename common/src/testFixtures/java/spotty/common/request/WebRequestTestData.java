package spotty.common.request;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import spotty.common.http.Headers;
import spotty.common.response.SpottyResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static java.lang.Integer.parseInt;
import static spotty.common.http.Headers.ACCEPT;
import static spotty.common.http.Headers.ACCEPT_ENCODING;
import static spotty.common.http.Headers.CONNECTION;
import static spotty.common.http.Headers.CONTENT_LENGTH;
import static spotty.common.http.Headers.CONTENT_TYPE;
import static spotty.common.http.Headers.HOST;
import static spotty.common.http.Headers.USER_AGENT;
import static spotty.common.http.HttpMethod.POST;

public interface WebRequestTestData {

    String requestBody = readFile("/request_body.txt");
    Headers headers = new Headers()
        .add(CONTENT_TYPE, "text/plain")
        .add(USER_AGENT, "Spotty Agent")
        .add(ACCEPT, "*/*")
        .add(HOST, "localhost:4000")
        .add(ACCEPT_ENCODING, "gzip, deflate, br")
        .add(CONNECTION, "keep-alive")
        .add(CONTENT_LENGTH, requestBody.length() + "");

    String requestHeaders = "POST / HTTP/1.1\n" + headers;

    String fullRequest = requestHeaders + "\n\n" + requestBody;

    default SpottyRequest.Builder aSpottyRequest() {
        final byte[] content = requestBody.getBytes();
        final Headers headers = this.headers.copy();

        return SpottyRequest.builder()
            .protocol("HTTP/1.1")
            .scheme("http")
            .method(POST)
            .path("/")
            .contentLength(parseInt(headers.remove(CONTENT_LENGTH)))
            .contentType(ContentType.parse(headers.remove(CONTENT_TYPE)))
            .headers(headers)
            .body(content);
    }

    default SpottyResponse aSpottyResponse(SpottyRequest request) {
        final SpottyResponse response = new SpottyResponse();
        response.contentType(request.contentType.get());
        response.body(request.body);

        return response;
    }

    @SuppressWarnings("all")
    static String readFile(String file) {
        try {
            return IOUtils.toString(WebRequestTestData.class.getResourceAsStream(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}