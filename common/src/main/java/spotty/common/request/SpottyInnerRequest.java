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
import java.util.Optional;
import java.util.Set;

@ToString
public final class SpottyInnerRequest implements SpottyRequest {
    private String protocol;
    private String scheme;
    private HttpMethod method;
    private String path;
    private QueryParams queryParams = QueryParams.EMPTY;
    private PathParams pathParams = PathParams.EMPTY;
    private int contentLength;
    private ContentType contentType;
    private byte[] body;

    private final Headers headers = new Headers();

    @Override
    public String protocol() {
        return protocol;
    }

    public SpottyInnerRequest protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    @Override
    public String scheme() {
        return scheme;
    }

    public SpottyInnerRequest scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    public SpottyInnerRequest method(HttpMethod method) {
        this.method = method;
        return this;
    }

    @Override
    public String path() {
        return path;
    }

    public SpottyInnerRequest path(String path) {
        this.path = path;
        return this;
    }

    @Override
    public int contentLength() {
        return contentLength;
    }

    public SpottyInnerRequest contentLength(int contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    @Override
    public ContentType contentType() {
        return contentType;
    }

    public SpottyInnerRequest contentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public Headers headers() {
        return headers;
    }

    public SpottyInnerRequest addHeader(String name, String value) {
        this.headers.add(name, value);
        return this;
    }

    public SpottyInnerRequest addHeaders(Headers headers) {
        this.headers.add(headers);
        return this;
    }

    @Override
    public Map<String, String> params() {
        return pathParams.params();
    }

    @Override
    public String param(String name) {
        return pathParams.param(name);
    }


    public PathParams pathParams() {
        return pathParams;
    }

    public SpottyInnerRequest pathParams(PathParams pathParams) {
        this.pathParams = pathParams;
        return this;
    }

    @Override
    public Map<String, Set<String>> queryParamsMap() {
        return queryParams.paramsMap();
    }

    @Override
    public Set<String> queryParams() {
        return queryParams.params();
    }

    public QueryParams queryParamsObject() {
        return queryParams;
    }

    public SpottyInnerRequest queryParams(QueryParams queryParams) {
        this.queryParams = queryParams;
        return this;
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
    public byte[] body() {
        return body;
    }

    public SpottyInnerRequest body(byte[] body) {
        this.body = body;
        return this;
    }

    @Override
    public <T> T parseBody(Class<T> clazz) {
        return Json.parse(body, clazz);
    }

    @Override
    public JsonNode parseBody() {
        return Json.parse(body);
    }

    public void reset() {
        protocol = null;
        scheme = null;
        method = null;
        path = null;
        queryParams = QueryParams.EMPTY;
        pathParams = PathParams.EMPTY;
        contentLength = 0;
        contentType = null;
        body = null;
        headers.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpottyInnerRequest that = (SpottyInnerRequest) o;

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