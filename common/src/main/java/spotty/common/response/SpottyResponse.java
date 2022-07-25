package spotty.common.response;

import spotty.common.cookie.Cookie;
import spotty.common.http.HttpHeaders;
import spotty.common.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static spotty.common.http.HttpStatus.OK;

public final class SpottyResponse {
    private static final String DEFAULT_CONTENT_TYPE = "text/plain";

    private final String protocol = "HTTP/1.1";

    private HttpStatus status = OK;
    private String contentType = DEFAULT_CONTENT_TYPE;
    private byte[] body;

    List<Cookie> cookies = emptyList();

    private final HttpHeaders headers = new HttpHeaders();

    public String protocol() {
        return protocol;
    }

    public HttpStatus status() {
        return status;
    }

    public SpottyResponse status(HttpStatus status) {
        this.status = status;
        return this;
    }

    public String contentType() {
        return contentType;
    }

    public SpottyResponse contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public byte[] body() {
        return body;
    }

    public String bodyAsString() {
        if (body == null) {
            return "";
        }

        return new String(body);
    }

    public SpottyResponse body(String body) {
        return body(body == null ? null : body.getBytes());
    }

    public SpottyResponse body(byte[] body) {
        this.body = body;
        return this;
    }

    public int contentLength() {
        return body == null ? 0 : body.length;
    }

    public HttpHeaders headers() {
        return headers;
    }

    public SpottyResponse addHeader(String name, String value) {
        this.headers.add(name, value);
        return this;
    }

    public SpottyResponse addHeaders(HttpHeaders headers) {
        this.headers.add(headers);
        return this;
    }

    public SpottyResponse replaceHeaders(HttpHeaders headers) {
        this.headers.clear();
        this.headers.add(headers);
        return this;
    }

    public SpottyResponse addCookie(Cookie cookie) {
        final List<Cookie> emptyCookies = emptyList();
        if (this.cookies == emptyCookies) {
            this.cookies = new ArrayList<>();
        }

        this.cookies.add(cookie);
        return this;
    }

    public SpottyResponse cookie(String name, String value) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .build());
    }

    public SpottyResponse cookie(String name, String value, int maxAge) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .maxAge(maxAge)
            .build());
    }

    public SpottyResponse cookie(String name, String value, int maxAge, boolean secured) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .maxAge(maxAge)
            .secure(secured)
            .build());
    }

    public SpottyResponse cookie(String name, String value, int maxAge, boolean secured, boolean httpOnly) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .maxAge(maxAge)
            .secure(secured)
            .httpOnly(httpOnly)
            .build());
    }

    public SpottyResponse cookie(String path, String name, String value, int maxAge, boolean secured) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .path(path)
            .maxAge(maxAge)
            .secure(secured)
            .build());
    }

    public SpottyResponse cookie(String path, String name, String value, int maxAge, boolean secured, boolean httpOnly) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .path(path)
            .maxAge(maxAge)
            .secure(secured)
            .httpOnly(httpOnly)
            .build());
    }

    public SpottyResponse cookie(String domain, String path, String name, String value, int maxAge, boolean secured, boolean httpOnly) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .domain(domain)
            .path(path)
            .maxAge(maxAge)
            .secure(secured)
            .httpOnly(httpOnly)
            .build());
    }

    public SpottyResponse removeCookie(String name) {
        return removeCookie(null, name);
    }

    public SpottyResponse removeCookie(String path, String name) {
        return addCookie(Cookie.builder()
            .name(name)
            .path(path)
            .maxAge(0)
            .build());
    }

    public void reset() {
        status = OK;
        contentType = DEFAULT_CONTENT_TYPE;
        body = null;
        headers.clear();
        cookies = emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpottyResponse that = (SpottyResponse) o;

        final String thisContentType = contentType != null ? contentType.toString() : null;
        final String thatContentType = that.contentType != null ? that.contentType.toString() : null;

        return Objects.equals(protocol, that.protocol)
            && status == that.status
            && Objects.equals(thisContentType, thatContentType)
            && Arrays.equals(body, that.body)
            && Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        int contentTypeHash = contentType != null ? contentType.toString().hashCode() : 0;
        int result = Objects.hash(protocol, status, contentTypeHash, headers);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }
}
