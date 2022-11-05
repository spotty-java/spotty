/*
 * Copyright 2022 - Alex Danilenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spotty.common.request;

import spotty.common.http.HttpHeaders;
import spotty.common.http.HttpMethod;
import spotty.common.http.HttpProtocol;
import spotty.common.request.params.PathParams;
import spotty.common.request.params.QueryParams;
import spotty.common.session.Session;
import spotty.common.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;
import static spotty.common.validation.Validation.notBlank;
import static spotty.common.validation.Validation.notNull;

public final class SpottyDefaultRequest implements SpottyRequest {
    private HttpProtocol protocol;
    private String scheme;
    private HttpMethod method;
    private String path;
    private QueryParams queryParams = QueryParams.EMPTY;
    private PathParams pathParams = PathParams.EMPTY;
    private int contentLength;
    private String contentType;
    private Supplier<String> host;
    private Supplier<String> ip;
    private IntSupplier port;
    private Map<String, String> cookies = emptyMap();
    private Session session;
    private InputStream body;
    private Object attachment;

    private final HttpHeaders headers = new HttpHeaders();

    @Override
    public HttpProtocol protocol() {
        return protocol;
    }

    public SpottyDefaultRequest protocol(HttpProtocol protocol) {
        this.protocol = notNull("protocol", protocol);
        return this;
    }

    @Override
    public String scheme() {
        return scheme;
    }

    public SpottyDefaultRequest scheme(String scheme) {
        this.scheme = notBlank("scheme", scheme);
        return this;
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    public SpottyDefaultRequest method(HttpMethod method) {
        this.method = notNull("method", method);
        return this;
    }

    @Override
    public String path() {
        return path;
    }

    public SpottyDefaultRequest path(String path) {
        this.path = notBlank("path", path);
        return this;
    }

    @Override
    public int contentLength() {
        return contentLength;
    }

    public SpottyDefaultRequest contentLength(int contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    public SpottyDefaultRequest contentType(String contentType) {
        this.contentType = notBlank("contentType", contentType);
        return this;
    }

    @Override
    public String host() {
        return host.get();
    }

    public SpottyDefaultRequest host(Supplier<String> host) {
        this.host = notNull("host", host);
        return this;
    }

    @Override
    public String ip() {
        return ip.get();
    }

    public SpottyDefaultRequest ip(Supplier<String> ip) {
        this.ip = notNull("ip", ip);
        return this;
    }

    @Override
    public int port() {
        return port.getAsInt();
    }

    public SpottyDefaultRequest port(IntSupplier port) {
        this.port = port;
        return this;
    }

    @Override
    public Map<String, String> cookies() {
        return cookies;
    }

    public SpottyDefaultRequest cookies(Map<String, String> cookies) {
        this.cookies = notNull("cookies", cookies);
        return this;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    public SpottyDefaultRequest addHeader(String name, String value) {
        this.headers.add(name, value);
        return this;
    }

    public SpottyDefaultRequest addHeaders(HttpHeaders headers) {
        this.headers.add(notNull("headers", headers));
        return this;
    }

    @Override
    public Map<String, String> pathParams() {
        return pathParams.params();
    }

    @Override
    public String pathParam(String name) {
        return pathParams.param(name);
    }

    public PathParams pathParamsObject() {
        return pathParams;
    }

    public SpottyDefaultRequest pathParamsObject(PathParams pathParams) {
        this.pathParams = notNull("pathParams", pathParams);
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

    public SpottyDefaultRequest queryParams(QueryParams queryParams) {
        this.queryParams = notNull("queryParams", queryParams);
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

    public SpottyDefaultRequest session(Session session) {
        this.session = notNull("session", session);
        return this;
    }

    @Override
    public Session session() {
        return session;
    }

    @Override
    public byte[] body() {
        return IOUtils.toByteArray(body);
    }

    @Override
    public String bodyAsString() {
        if (body == null) {
            return "";
        }

        return IOUtils.toString(body);
    }

    @Override
    public InputStream bodyAsStream() {
        return body;
    }

    public SpottyDefaultRequest body(byte[] body) {
        this.body = notNull("body", new ByteArrayInputStream(body));
        return this;
    }

    public SpottyDefaultRequest body(InputStream body) {
        this.body = notNull("body", body);
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
        host = null;
        ip = null;
        port = null;
        body = null;
        headers.clear();
        cookies = emptyMap();
        session = null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[\n" +
            "protocol=" + protocol + '\n' +
            "method=" + method + '\n' +
            "path=" + path + '\n' +
            "queryParams=" + queryParams + '\n' +
            "pathParams=" + pathParams + '\n' +
            "contentLength=" + contentLength + '\n' +
            "contentType=" + contentType + '\n' +
            "host=" + (host == null ? "" : host.get()) + '\n' +
            "ip=" + (ip == null ? "" : ip.get()) + '\n' +
            "port=" + (port == null ? "" : port.getAsInt()) + '\n' +
            "cookies=" + cookies + '\n' +
            "session=" + session + '\n' +
            "attachment=" + attachment + '\n' +
            "headers={\n" + headers + "\n}\n" +
            "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpottyDefaultRequest that = (SpottyDefaultRequest) o;

        final String host = this.host == null ? null : this.host.get();
        final String ip = this.ip == null ? null : this.ip.get();
        final int port = this.port == null ? 0 : this.port.getAsInt();

        final String thatHost = this.host == null ? null : this.host.get();
        final String thatIp = this.ip == null ? null : this.ip.get();
        final int thatPort = this.port == null ? 0 : this.port.getAsInt();

        return contentLength == that.contentLength
            && Objects.equals(protocol, that.protocol)
            && Objects.equals(scheme, that.scheme)
            && method == that.method
            && Objects.equals(queryParams, that.queryParams)
            && Objects.equals(pathParams, that.pathParams)
            && Objects.equals(path, that.path)
            && Objects.equals(contentType, that.contentType)
            && Objects.equals(host, thatHost)
            && Objects.equals(ip, thatIp)
            && port == thatPort
            && Objects.equals(headers, that.headers)
            && Objects.equals(cookies, that.cookies)
            && Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        final String host = this.host == null ? null : this.host.get();
        final String ip = this.ip == null ? null : this.ip.get();
        final int port = this.port == null ? 0 : this.port.getAsInt();

        return Objects.hash(protocol, scheme, method, path, queryParams, pathParams, contentLength, contentType, host, ip, port, headers, cookies, session);
    }

}
