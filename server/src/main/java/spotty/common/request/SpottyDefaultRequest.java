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

public final class SpottyDefaultRequest implements SpottyRequest {
    private final String protocol;
    private final String scheme;
    private final HttpMethod method;
    private final String path;
    private final QueryParams queryParams;
    private final PathParams pathParams;
    private final int contentLength;
    private final String contentType;
    private final String host;
    private final String ip;
    private final int port;
    private final HttpHeaders headers;
    private final Map<String, String> cookies;
    private final Session session;
    private final byte[] body;

    private Object attachment;

    public SpottyDefaultRequest(SpottyInnerRequest request) {
        this.protocol = request.protocol();
        this.scheme = request.scheme();
        this.method = request.method();
        this.path = request.path();
        this.queryParams = request.queryParamsObject();
        this.pathParams = request.pathParams();
        this.contentLength = request.contentLength();
        this.contentType = request.contentType();
        this.host = request.host();
        this.ip = request.ip();
        this.port = request.port();
        this.headers = request.headers().copy();
        this.cookies = request.cookies();
        this.session = request.session();
        this.body = request.body();
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
    public String contentType() {
        return contentType;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public String ip() {
        return ip;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public Map<String, String> cookies() {
        return cookies;
    }

    @Override
    public void attach(Object attachment) {
        this.attachment = attachment;
    }

    @Override
    public Object attachment() {
        return attachment;
    }

    @Override
    public byte[] body() {
        return body;
    }

    @Override
    public Session session() {
        return session;
    }

    @Override
    public HttpHeaders headers() {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpottyDefaultRequest that = (SpottyDefaultRequest) o;

        return contentLength == that.contentLength
            && Objects.equals(protocol, that.protocol)
            && Objects.equals(scheme, that.scheme)
            && method == that.method
            && Objects.equals(queryParams, that.queryParams)
            && Objects.equals(pathParams, that.pathParams)
            && Objects.equals(path, that.path)
            && Objects.equals(contentType, that.contentType)
            && Objects.equals(host, that.host)
            && Objects.equals(ip, that.ip)
            && port == that.port
            && Arrays.equals(body, that.body)
            && Objects.equals(headers, that.headers)
            && Objects.equals(attachment, that.attachment)
            && Objects.equals(session, that.session)
            && Objects.equals(cookies, that.cookies);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(protocol, scheme, method, path, queryParams, pathParams, contentLength, contentType, host, ip, port, headers, attachment, session, cookies);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }

}
