package spotty.server.request;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.entity.ContentType;
import spotty.server.json.Json;
import spotty.server.stream.SpottyInputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpottyRequest {
    public final String protocol;
    public final String scheme;
    public final String method;
    public final String path;
    public final long contentLength;
    public final Optional<ContentType> contentType;
    public final SpottyInputStream body;
    public final Map<String, String> headers;

    public SpottyRequest(Builder builder) {
        this.protocol = builder.protocol;
        this.scheme = builder.scheme;
        this.method = builder.method;
        this.path = builder.path;
        this.contentLength = builder.contentLength;
        this.contentType = builder.contentType;
        this.body = builder.body;
        this.headers = Map.copyOf(builder.headers);
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
        private String protocol;
        private String scheme;
        private String method;
        private String path;
        private long contentLength;
        private Optional<ContentType> contentType = Optional.empty();
        private SpottyInputStream body;

        private final Map<String, String> headers = new HashMap<>();

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder method(String method) {
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

        public Builder contentLength(long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public Builder contentType(ContentType contentType) {
            this.contentType = Optional.ofNullable(contentType);
            return this;
        }

        public Builder body(SpottyInputStream body) {
            this.body = body;
            return this;
        }

        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            headers.putAll(headers);
            return this;
        }

        public SpottyRequest build() {
            return new SpottyRequest(this);
        }
    }
}
