package spotty.common.request;

import spotty.common.http.HttpHeaders;
import spotty.common.response.SpottyResponse;
import spotty.common.utils.IOUtils;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static spotty.common.http.HttpHeaders.ACCEPT;
import static spotty.common.http.HttpHeaders.ACCEPT_ENCODING;
import static spotty.common.http.HttpHeaders.CONNECTION;
import static spotty.common.http.HttpHeaders.CONTENT_LENGTH;
import static spotty.common.http.HttpHeaders.CONTENT_TYPE;
import static spotty.common.http.HttpHeaders.HOST;
import static spotty.common.http.HttpHeaders.USER_AGENT;
import static spotty.common.http.HttpMethod.POST;

public interface WebRequestTestData {

    String requestBody = readFile("/request_body.txt");
    HttpHeaders headers = new HttpHeaders()
        .add(CONTENT_TYPE, "text/plain")
        .add(USER_AGENT, "Spotty Agent")
        .add(ACCEPT, "*/*")
        .add(HOST, "localhost:4000")
        .add(ACCEPT_ENCODING, "gzip, deflate, br")
        .add(CONNECTION, "keep-alive")
        .add(CONTENT_LENGTH, requestBody.length() + "");

    Map<String, String> cookies = new HashMap<String, String>(){{
        put("name", "John");
        put("lastName", "Doe");
    }};

    String requestHeaders = "POST / HTTP/1.1\n" + headers;

    String fullRequest = requestHeaders + "\n\n" + requestBody;

    default SpottyInnerRequest aSpottyRequest() {
        final byte[] content = requestBody.getBytes();
        final HttpHeaders headers = this.headers.copy();

        final SpottyInnerRequest request = new SpottyInnerRequest();
        return request
            .protocol("HTTP/1.1")
            .scheme("http")
            .method(POST)
            .path("/")
            .contentLength(parseInt(headers.remove(CONTENT_LENGTH)))
            .contentType(headers.remove(CONTENT_TYPE))
            .addHeaders(headers)
            .cookies(cookies)
            .body(content);
    }

    default SpottyResponse aSpottyResponse(SpottyRequest request) {
        final SpottyResponse response = new SpottyResponse();
        response.contentType(request.contentType());
        response.body(request.body());

        return response;
    }

    @SuppressWarnings("all")
    static String readFile(String file) {
        return IOUtils.toString(WebRequestTestData.class.getResourceAsStream(file));
    }
}