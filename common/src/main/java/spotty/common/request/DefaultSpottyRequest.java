package spotty.common.request;

import spotty.common.http.HttpHeaders;
import spotty.common.http.HttpMethod;
import spotty.common.request.params.PathParams;
import spotty.common.request.params.QueryParams;
import spotty.common.session.Session;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static spotty.common.validation.Validation.notNull;

public final class DefaultSpottyRequest implements SpottyRequest {
    private String protocol;
    private String scheme;
    private HttpMethod method;
    private String path;
    private QueryParams queryParams = QueryParams.EMPTY;
    private PathParams pathParams = PathParams.EMPTY;
    private int contentLength;
    private String contentType;
    private Map<String, String> cookies = emptyMap();
    private Object attachment;
    private Session session;
    private byte[] body;

    private final HttpHeaders headers = new HttpHeaders();

    @Override
    public String protocol() {
        return protocol;
    }

    public DefaultSpottyRequest protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    @Override
    public String scheme() {
        return scheme;
    }

    public DefaultSpottyRequest scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    public DefaultSpottyRequest method(HttpMethod method) {
        this.method = method;
        return this;
    }

    @Override
    public String path() {
        return path;
    }

    public DefaultSpottyRequest path(String path) {
        this.path = path;
        return this;
    }

    @Override
    public int contentLength() {
        return contentLength;
    }

    public DefaultSpottyRequest contentLength(int contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    public DefaultSpottyRequest contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public Map<String, String> cookies() {
        return cookies;
    }

    public DefaultSpottyRequest cookies(Map<String, String> cookies) {
        this.cookies = notNull("cookies", cookies);
        return this;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    public DefaultSpottyRequest addHeader(String name, String value) {
        this.headers.add(name, value);
        return this;
    }

    public DefaultSpottyRequest addHeaders(HttpHeaders headers) {
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

    public DefaultSpottyRequest pathParams(PathParams pathParams) {
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

    public DefaultSpottyRequest queryParams(QueryParams queryParams) {
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
    public void attach(Object attachment) {
        this.attachment = attachment;
    }

    @Override
    public Object attachment() {
        return attachment;
    }

    public DefaultSpottyRequest session(Session session) {
        this.session = session;
        return this;
    }

    @Override
    public Session session() {
        return session;
    }

    @Override
    public byte[] body() {
        return body;
    }

    public DefaultSpottyRequest body(byte[] body) {
        this.body = body;
        return this;
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
        attachment = null;
        headers.clear();
        cookies = emptyMap();
        session = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultSpottyRequest that = (DefaultSpottyRequest) o;

        return contentLength == that.contentLength
            && Objects.equals(protocol, that.protocol)
            && Objects.equals(scheme, that.scheme)
            && method == that.method
            && Objects.equals(queryParams, that.queryParams)
            && Objects.equals(pathParams, that.pathParams)
            && Objects.equals(path, that.path)
            && Objects.equals(contentType, that.contentType)
            && Arrays.equals(body, that.body)
            && Objects.equals(headers, that.headers)
            && Objects.equals(attachment, that.attachment)
            && Objects.equals(cookies, that.cookies)
            && Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(protocol, scheme, method, path, queryParams, pathParams, contentLength, contentType, headers, attachment, cookies, session);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }

}
