package spotty.common.response;

import lombok.ToString;
import org.apache.http.entity.ContentType;
import spotty.common.http.Headers;
import spotty.common.http.HttpStatus;

import java.util.Arrays;
import java.util.Objects;

import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static spotty.common.http.HttpStatus.OK;

@ToString
public final class SpottyResponse {
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

    public void reset() {
        status = OK;
        contentType = TEXT_PLAIN;
        body = null;
        headers.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpottyResponse that = (SpottyResponse) o;

        final var thisContentType = contentType != null ? contentType.toString() : null;
        final var thatContentType = that.contentType != null ? that.contentType.toString() : null;

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
