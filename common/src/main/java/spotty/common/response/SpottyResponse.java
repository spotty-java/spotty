package spotty.common.response;

import org.apache.http.entity.ContentType;
import spotty.common.http.Headers;
import spotty.common.http.HttpStatus;

import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static spotty.common.http.HttpStatus.OK;

public class SpottyResponse {
    private final String protocol = "HTTP/1.1";

    private HttpStatus status = OK;
    private ContentType contentType = TEXT_PLAIN;
    private byte[] body;

    private final Headers headers = new Headers();

    public String protocol() {
        return protocol;
    }

    public HttpStatus status() {
        return status;
    }

    public void status(HttpStatus status) {
        this.status = status;
    }

    public ContentType contentType() {
        return contentType;
    }

    public void contentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public byte[] body() {
        return body;
    }

    public void body(byte[] body) {
        this.body = body;
    }

    public int contentLength() {
        return body == null ? 0 : body.length;
    }

    public Headers headers() {
        return headers;
    }

    public void addHeader(String name, String value) {
        this.headers.add(name, value);
    }

    public void addHeaders(Headers headers) {
        this.headers.add(headers);
    }

    public void replaceHeaders(Headers headers) {
        this.headers.clear();
        this.headers.add(headers);
    }
}
