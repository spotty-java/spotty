package spotty.common.request;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.entity.ContentType;
import spotty.common.http.Headers;
import spotty.common.http.HttpMethod;
import spotty.common.json.Json;

import java.util.Optional;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

public class SpottyRequest {
    public final String protocol;
    public final String scheme;
    public final HttpMethod method;
    public final String path;
    public final long contentLength;
    public final Optional<ContentType> contentType;
    public final byte[] body;
    public final Headers headers;

    public SpottyRequest(Builder builder) {
        this.protocol = notBlank(builder.protocol, "builder.protocol");
        this.scheme = notBlank(builder.scheme, "builder.scheme");
        this.method = notNull(builder.method, "builder.method");
        this.path = notBlank(builder.path, "builder.path");
        this.contentLength = builder.contentLength;
        this.contentType = builder.contentType;
        this.body = notNull(builder.body, "builder.body");
        this.headers = notNull(builder.headers, "builder.headers").copy();
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

        public Builder clear() {
            protocol = null;
            scheme = null;
            method = null;
            path = null;
            contentLength = 0;
            contentType = Optional.empty();
            body = null;
            headers.clear();

            return this;
        }

        public SpottyRequest build() {
            return new SpottyRequest(this);
        }
    }
}
