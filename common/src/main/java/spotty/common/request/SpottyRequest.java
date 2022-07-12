package spotty.common.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.ToString;
import org.apache.http.entity.ContentType;
import spotty.common.http.Headers;
import spotty.common.http.HttpMethod;
import spotty.common.json.Json;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@ToString
public final class SpottyRequest {
    public final String protocol;
    public final String scheme;
    public final HttpMethod method;
    public final String path;
    public final int contentLength;
    public final Optional<ContentType> contentType;
    public final byte[] body;
    public final Headers headers;

    public SpottyRequest(Builder builder) {
        this.protocol = builder.protocol;
        this.scheme = builder.scheme;
        this.method = builder.method;
        this.path = builder.path;
        this.contentLength = builder.contentLength;
        this.contentType = builder.contentType;
        this.body = builder.body;
        this.headers = builder.headers.copy();
    }

    public <T> T parseBody(Class<T> clazz) {
        return Json.parse(body, clazz);
    }

    public JsonNode parseBody() {
        return Json.parse(body);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpottyRequest that = (SpottyRequest) o;

        final String thisContentType = contentType.map(ContentType::toString).orElse("");
        final String thatContentType = that.contentType.map(ContentType::toString).orElse("");

        return contentLength == that.contentLength
            && Objects.equals(protocol, that.protocol)
            && Objects.equals(scheme, that.scheme)
            && method == that.method
            && Objects.equals(path, that.path)
            && Objects.equals(thisContentType, thatContentType)
            && Arrays.equals(body, that.body)
            && Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        int contentTypeHash = contentType.map(ContentType::toString).map(String::hashCode).orElse(0);
        int result = Objects.hash(protocol, scheme, method, path, contentLength, contentTypeHash, headers);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }

    public static class Builder {
        public String protocol;
        public String scheme;
        public HttpMethod method;
        public String path;
        public int contentLength;
        public Optional<ContentType> contentType = Optional.empty();
        public byte[] body;

        private final Headers headers = new Headers();

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder contentLength(int contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public Builder contentType(ContentType contentType) {
            this.contentType = Optional.ofNullable(contentType);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder header(String name, String value) {
            headers.add(name, value);
            return this;
        }

        public Builder headers(Headers headers) {
            this.headers.clear();
            this.headers.add(headers);
            return this;
        }

        public void clear() {
            protocol = null;
            scheme = null;
            method = null;
            path = null;
            contentLength = 0;
            contentType = Optional.empty();
            body = null;
            headers.clear();
        }

        public SpottyRequest build() {
            return new SpottyRequest(this);
        }
    }
}
