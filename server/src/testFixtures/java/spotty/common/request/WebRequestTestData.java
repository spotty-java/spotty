package spotty.common.request;

import spotty.common.http.HttpHeaders;
import spotty.common.response.SpottyResponse;
import spotty.common.utils.IOUtils;

import static java.lang.Integer.parseInt;
import static spotty.common.http.HttpHeaders.ACCEPT;
import static spotty.common.http.HttpHeaders.ACCEPT_ENCODING;
import static spotty.common.http.HttpHeaders.CONNECTION;
import static spotty.common.http.HttpHeaders.CONTENT_LENGTH;
import static spotty.common.http.HttpHeaders.CONTENT_TYPE;
import static spotty.common.http.HttpHeaders.COOKIE;
import static spotty.common.http.HttpHeaders.HOST;
import static spotty.common.http.HttpHeaders.USER_AGENT;
import static spotty.common.http.HttpMethod.POST;
import static spotty.common.utils.HeaderUtils.parseCookies;

public interface WebRequestTestData {

    String requestBody = readFile("/request_body.txt");
    HttpHeaders headers = new HttpHeaders()
        .add(CONTENT_TYPE, "text/plain")
        .add(USER_AGENT, "Spotty Agent")
        .add(ACCEPT, "*/*")
        .add(HOST, "localhost:4000")
        .add(ACCEPT_ENCODING, "gzip, deflate, br")
        .add(CONNECTION, "keep-alive")
        .add(COOKIE, "name=John; lastName=Doe")
        .add(CONTENT_LENGTH, requestBody.length() + "");

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
            .cookies(parseCookies(headers.remove(COOKIE)))
            .addHeaders(headers)
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