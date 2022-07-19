package spotty.common.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.ToString;
import org.apache.http.entity.ContentType;
import spotty.common.http.Headers;
import spotty.common.http.HttpMethod;
import spotty.common.json.Json;
import spotty.common.request.params.PathParams;
import spotty.common.request.params.QueryParams;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@ToString
public final class SpottyDefaultRequest implements SpottyRequest {
    private final String protocol;
    private final String scheme;
    private final HttpMethod method;
    private final String path;
    private final QueryParams queryParams;
    private final PathParams pathParams;
    private final int contentLength;
    private final ContentType contentType;
    private final Headers headers;
    private final byte[] body;

    public SpottyDefaultRequest(SpottyInnerRequest request) {
        this.protocol = request.protocol();
        this.scheme = request.scheme();
        this.method = request.method();
        this.path = request.path();
        this.queryParams = request.queryParamsObject();
        this.pathParams = request.pathParams();
        this.contentLength = request.contentLength();
        this.contentType = request.contentType();
        this.body = request.body();
        this.headers = request.headers().copy();
    }

    @Override
    public String protocol() {
        return protocol;
    }

    @Override
    public String scheme() {
        return scheme;
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public Map<String, Set<String>> queryParamsMap() {
        return queryParams.paramsMap();
    }

    @Override
    public Set<String> queryParams() {
        return queryParams.params();
    }

    @Override
    public Set<String> queryParams(String name) {
        return queryParams.params(name);
    }

    @Override
    public String queryParam(String name) {
        return queryParams.param(name);
    }

    @Override
    public int contentLength() {
        return contentLength;
    }

    @Override
    public ContentType contentType() {
        return contentType;
    }

    @Override
    public byte[] body() {
        return body;
    }

    @Override
    public Headers headers() {
        return headers.copy();
    }

    @Override
    public Map<String, String> params() {
        return pathParams.params();
    }

    @Override
    public String param(String name) {
        return pathParams.param(name);
    }

    @Override
    public <T> T parseBody(Class<T> clazz) {
        return Json.parse(body, clazz);
    }

    @Override
    public JsonNode parseBody() {
        return Json.parse(body);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpottyDefaultRequest that = (SpottyDefaultRequest) o;

        final String contentMimeType = contentType == null ? null : contentType.getMimeType();
        final String thatContentMimeType = that.contentType == null ? null : that.contentType.getMimeType();

        return contentLength == that.contentLength
            && Objects.equals(protocol, that.protocol)
            && Objects.equals(scheme, that.scheme)
            && method == that.method
            && Objects.equals(queryParams, that.queryParams)
            && Objects.equals(pathParams, that.pathParams)
            && Objects.equals(path, that.path)
            && Objects.equals(contentMimeType, thatContentMimeType)
            && Arrays.equals(body, that.body)
            && Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        final String contentMimeType = contentType == null ? null : contentType.getMimeType();
        int result = Objects.hash(protocol, scheme, method, path, queryParams, pathParams, contentLength, contentMimeType, headers);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }

}
