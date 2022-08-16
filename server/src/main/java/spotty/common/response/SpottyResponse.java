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
package spotty.common.response;

import spotty.common.cookie.Cookie;
import spotty.common.exception.SpottyHttpException;
import spotty.common.http.HttpHeaders;
import spotty.common.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static spotty.Spotty.PROTOCOL_SUPPORT;
import static spotty.common.http.ConnectionValue.CLOSE;
import static spotty.common.http.HttpHeaders.CONNECTION;
import static spotty.common.http.HttpHeaders.LOCATION;
import static spotty.common.http.HttpStatus.MOVED_PERMANENTLY;
import static spotty.common.http.HttpStatus.OK;
import static spotty.common.validation.Validation.validate;

public final class SpottyResponse {
    private static final String DEFAULT_CONTENT_TYPE = "text/plain";

    private final String protocol = PROTOCOL_SUPPORT;

    private HttpStatus status = OK;
    private String contentType = DEFAULT_CONTENT_TYPE;
    private byte[] body;

    private List<Cookie> cookies = emptyList();

    private final HttpHeaders headers = new HttpHeaders();

    public String protocol() {
        return protocol;
    }

    /**
     * @return http status
     */
    public HttpStatus status() {
        return status;
    }

    /**
     * Sets HTTP status for response
     *
     * @param status http status
     * @return Response object
     */
    public SpottyResponse status(HttpStatus status) {
        this.status = status;
        return this;
    }

    /**
     * @return content-type
     */
    public String contentType() {
        return contentType;
    }

    /**
     * Sets http content-type for response
     *
     * @param contentType http content-type
     * @return Response object
     */
    public SpottyResponse contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * @return body as bytes array
     */
    public byte[] body() {
        return body;
    }

    /**
     * @return body as string
     */
    public String bodyAsString() {
        if (body == null) {
            return "";
        }

        return new String(body);
    }

    /**
     * Sets body
     *
     * @param body string content
     * @return Response object
     */
    public SpottyResponse body(String body) {
        return body(body == null ? null : body.getBytes(UTF_8));
    }

    /**
     * Sets body
     *
     * @param body bytes array
     * @return Response object
     */
    public SpottyResponse body(byte[] body) {
        this.body = body;
        return this;
    }

    /**
     * @return content-length
     */
    public int contentLength() {
        return body == null ? 0 : body.length;
    }

    /**
     * @return all http headers
     */
    public HttpHeaders headers() {
        return headers;
    }

    /**
     * Add header to response
     *
     * @param name  name of header
     * @param value value of header
     * @return Response object
     */
    public SpottyResponse addHeader(String name, String value) {
        this.headers.add(name, value);
        return this;
    }

    /**
     * Add headers to response
     *
     * @param headers all headers to add
     * @return Response object
     */
    public SpottyResponse addHeaders(HttpHeaders headers) {
        this.headers.add(headers);
        return this;
    }

    /**
     * Replace all headers to given
     *
     * @param headers headers replacement
     * @return Response object
     */
    public SpottyResponse replaceHeaders(HttpHeaders headers) {
        this.headers.clear();
        this.headers.add(headers);
        return this;
    }

    /**
     * @return all cookies
     */
    public List<Cookie> cookies() {
        return cookies;
    }

    /**
     * Add cookie to response
     *
     * @param cookie object to add
     * @return Response object
     */
    public SpottyResponse addCookie(Cookie cookie) {
        final List<Cookie> emptyCookies = emptyList();
        if (this.cookies == emptyCookies) {
            this.cookies = new ArrayList<>();
        }

        this.cookies.add(cookie);
        return this;
    }

    /**
     * Adds not persistent cookie to the response.
     * Can be invoked multiple times to insert more than one cookie.
     *
     * @param name  name of the cookie
     * @param value value of the cookie
     */
    public SpottyResponse cookie(String name, String value) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .build());
    }

    /**
     * Adds not persistent cookie to the response.
     * Can be invoked multiple times to insert more than one cookie.
     *
     * @param name   name of the cookie
     * @param value  value of the cookie
     * @param maxAge max age of the cookie in seconds (zero - deletes the cookie)
     */
    public SpottyResponse cookie(String name, String value, int maxAge) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .maxAge(maxAge)
            .build());
    }

    /**
     * Adds not persistent cookie to the response.
     * Can be invoked multiple times to insert more than one cookie.
     *
     * @param name    name of the cookie
     * @param value   value of the cookie
     * @param maxAge  max age of the cookie in seconds (zero - deletes the cookie)
     * @param secured if true : cookie will be secured
     */
    public SpottyResponse cookie(String name, String value, int maxAge, boolean secured) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .maxAge(maxAge)
            .secure(secured)
            .build());
    }

    /**
     * Adds not persistent cookie to the response.
     * Can be invoked multiple times to insert more than one cookie.
     *
     * @param name     name of the cookie
     * @param value    value of the cookie
     * @param maxAge   max age of the cookie in seconds (zero - deletes the cookie)
     * @param secured  if true : cookie will be secured
     * @param httpOnly if true: cookie will be marked as http only
     */
    public SpottyResponse cookie(String name, String value, int maxAge, boolean secured, boolean httpOnly) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .maxAge(maxAge)
            .secure(secured)
            .httpOnly(httpOnly)
            .build());
    }

    /**
     * Adds not persistent cookie to the response.
     * Can be invoked multiple times to insert more than one cookie.
     *
     * @param path    path of the cookie
     * @param name    name of the cookie
     * @param value   value of the cookie
     * @param maxAge  max age of the cookie in seconds (zero - deletes the cookie)
     * @param secured if true : cookie will be secured
     */
    public SpottyResponse cookie(String path, String name, String value, int maxAge, boolean secured) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .path(path)
            .maxAge(maxAge)
            .secure(secured)
            .build());
    }

    /**
     * Adds not persistent cookie to the response.
     * Can be invoked multiple times to insert more than one cookie.
     *
     * @param path     path of the cookie
     * @param name     name of the cookie
     * @param value    value of the cookie
     * @param maxAge   max age of the cookie in seconds (zero - deletes the cookie)
     * @param secured  if true : cookie will be secured
     * @param httpOnly if true: cookie will be marked as http only
     */
    public SpottyResponse cookie(String path, String name, String value, int maxAge, boolean secured, boolean httpOnly) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .path(path)
            .maxAge(maxAge)
            .secure(secured)
            .httpOnly(httpOnly)
            .build());
    }

    /**
     * Adds not persistent cookie to the response.
     * Can be invoked multiple times to insert more than one cookie.
     *
     * @param domain   domain of the cookie
     * @param path     path of the cookie
     * @param name     name of the cookie
     * @param value    value of the cookie
     * @param maxAge   max age of the cookie in seconds (zero - deletes the cookie)
     * @param secured  if true : cookie will be secured
     * @param httpOnly if true: cookie will be marked as http only
     */
    public SpottyResponse cookie(String domain, String path, String name, String value, int maxAge, boolean secured, boolean httpOnly) {
        return addCookie(Cookie.builder()
            .name(name)
            .value(value)
            .domain(domain)
            .path(path)
            .maxAge(maxAge)
            .secure(secured)
            .httpOnly(httpOnly)
            .build());
    }

    /**
     * Removes the cookie.
     *
     * @param name name of the cookie
     */
    public SpottyResponse removeCookie(String name) {
        return removeCookie(null, name);
    }

    /**
     * Removes the cookie.
     *
     * @param path path of the cookie
     * @param name name of the cookie
     */
    public SpottyResponse removeCookie(String path, String name) {
        return addCookie(
            Cookie.builder()
                .name(name)
                .path(path)
                .maxAge(0)
                .build()
        );
    }

    /**
     * Trigger a browser redirect
     *
     * @param location where to redirect permanently
     */
    public void redirect(String location) {
        redirect(location, MOVED_PERMANENTLY);
    }

    /**
     * Trigger a browser redirect with specific http 3XX status code.
     *
     * @param location where to redirect
     * @param status   the http status code
     */
    public void redirect(String location, HttpStatus status) {
        validate(status.is3xxRedirection(), "redirection statuses allowed only");

        this.status = status;
        headers.add(LOCATION, location);

        // if path starts from "http" more likely it means that
        // client will be redirected to different server, so we can close the connection
        if (location.startsWith("http")) {
            headers.add(CONNECTION, CLOSE.code);
        }

        // throw an exception to stop execution of router handler
        throw new SpottyHttpException(status);
    }

    /**
     * reset to empty response
     */
    public void reset() {
        status = OK;
        contentType = DEFAULT_CONTENT_TYPE;
        body = null;
        headers.clear();
        cookies = emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SpottyResponse that = (SpottyResponse) o;

        return Objects.equals(protocol, that.protocol)
            && status == that.status
            && Objects.equals(contentType, that.contentType)
            && Arrays.equals(body, that.body)
            && Objects.equals(cookies, that.cookies)
            && Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(protocol, status, contentType, cookies, headers);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }
}
