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
package spotty.common.http;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static spotty.common.validation.Validation.isNull;

public final class HttpHeaders {
    /**
     * RFC 2616 (HTTP/1.1) Section 14.1
     */
    public static final String ACCEPT = "accept";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.2
     */
    public static final String ACCEPT_CHARSET = "accept-charset";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.3
     */
    public static final String ACCEPT_ENCODING = "accept-encoding";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.4
     */
    public static final String ACCEPT_LANGUAGE = "accept-language";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.5
     */
    public static final String ACCEPT_RANGES = "accept-ranges";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.6
     */
    public static final String AGE = "age";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.1, RFC 2616 (HTTP/1.1) Section 14.7
     */
    public static final String ALLOW = "allow";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.2, RFC 2616 (HTTP/1.1) Section 14.8
     */
    public static final String AUTHORIZATION = "authorization";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.9
     */
    public static final String CACHE_CONTROL = "cache-control";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.10
     */
    public static final String CONNECTION = "connection";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.3, RFC 2616 (HTTP/1.1) Section 14.11
     */
    public static final String CONTENT_ENCODING = "content-encoding";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.12
     */
    public static final String CONTENT_LANGUAGE = "content-language";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.4, RFC 2616 (HTTP/1.1) Section 14.13
     */
    public static final String CONTENT_LENGTH = "content-length";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.14
     */
    public static final String CONTENT_LOCATION = "content-location";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.15
     */
    public static final String CONTENT_MD5 = "content-md5";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.16
     */
    public static final String CONTENT_RANGE = "content-range";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.5, RFC 2616 (HTTP/1.1) Section 14.17
     */
    public static final String CONTENT_TYPE = "content-type";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.6, RFC 2616 (HTTP/1.1) Section 14.18
     */
    public static final String DATE = "date";

    /**
     * RFC 2518 (WevDAV) Section 9.1
     */
    public static final String DAV = "dav";

    /**
     * RFC 2518 (WevDAV) Section 9.2
     */
    public static final String DEPTH = "depth";

    /**
     * RFC 2518 (WevDAV) Section 9.3
     */
    public static final String DESTINATION = "destination";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.19
     */
    public static final String ETAG = "etag";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.20
     */
    public static final String EXPECT = "expect";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.7, RFC 2616 (HTTP/1.1) Section 14.21
     */
    public static final String EXPIRES = "expires";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.8, RFC 2616 (HTTP/1.1) Section 14.22
     */
    public static final String FROM = "from";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.23
     */
    public static final String HOST = "host";

    /**
     * RFC 2518 (WevDAV) Section 9.4
     */
    public static final String IF = "if";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.24
     */
    public static final String IF_MATCH = "if-match";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.9, RFC 2616 (HTTP/1.1) Section 14.25
     */
    public static final String IF_MODIFIED_SINCE = "if-modified-since";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.26
     */
    public static final String IF_NONE_MATCH = "if-none-match";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.27
     */
    public static final String IF_RANGE = "if-range";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.28
     */
    public static final String IF_UNMODIFIED_SINCE = "if-unmodified-since";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.10, RFC 2616 (HTTP/1.1) Section 14.29
     */
    public static final String LAST_MODIFIED = "last-modified";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.11, RFC 2616 (HTTP/1.1) Section 14.30
     */
    public static final String LOCATION = "location";

    /**
     * RFC 2518 (WevDAV) Section 9.5
     */
    public static final String LOCK_TOKEN = "lock-token";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.31
     */
    public static final String MAX_FORWARDS = "max-forwards";

    /**
     * RFC 2518 (WevDAV) Section 9.6
     */
    public static final String OVERWRITE = "overwrite";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.12, RFC 2616 (HTTP/1.1) Section 14.32
     */
    public static final String PRAGMA = "pragma";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.33
     */
    public static final String PROXY_AUTHENTICATE = "proxy-authenticate";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.34
     */
    public static final String PROXY_AUTHORIZATION = "proxy-authorization";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.35
     */
    public static final String RANGE = "range";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.13, RFC 2616 (HTTP/1.1) Section 14.36
     */
    public static final String REFERER = "referer";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.37
     */
    public static final String RETRY_AFTER = "retry-after";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.14, RFC 2616 (HTTP/1.1) Section 14.38
     */
    public static final String SERVER = "server";

    /**
     * RFC 2518 (WevDAV) Section 9.7
     */
    public static final String STATUS_URI = "status-uri";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.39
     */
    public static final String TE = "te";

    /**
     * RFC 2518 (WevDAV) Section 9.8
     */
    public static final String TIMEOUT = "timeout";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.40
     */
    public static final String TRAILER = "trailer";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.41
     */
    public static final String TRANSFER_ENCODING = "transfer-encoding";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.42
     */
    public static final String UPGRADE = "upgrade";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.15, RFC 2616 (HTTP/1.1) Section 14.43
     */
    public static final String USER_AGENT = "user-agent";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.44
     */
    public static final String VARY = "vary";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.45
     */
    public static final String VIA = "via";

    /**
     * RFC 2616 (HTTP/1.1) Section 14.46
     */
    public static final String WARNING = "warning";

    /**
     * RFC 1945 (HTTP/1.0) Section 10.16, RFC 2616 (HTTP/1.1) Section 14.47
     */
    public static final String WWW_AUTHENTICATE = "www-authenticate";

    public static final String COOKIE = "cookie";

    public static final String SET_COOKIE = "set-cookie";

    public static final String SPOTTY_SESSION_ID = "SSID";

    private final Map<String, String> headers = new HashMap<>();

    public HttpHeaders() {
    }

    public HttpHeaders(HttpHeaders headers) {
        this.headers.putAll(headers.headers);
    }

    /**
     * add header
     *
     * @param name header name
     * @param value header value
     * @return this instance of headers
     */
    public HttpHeaders add(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * add a bunch of headers
     *
     * @param headers bunch of headers
     * @return this instance of headers
     */
    public HttpHeaders add(HttpHeaders headers) {
        this.headers.putAll(headers.headers);
        return this;
    }

    /**
     * add a bunch of headers
     *
     * @param headers bunch of headers
     * @return this instance of headers
     */
    public HttpHeaders add(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    /**
     * Returns the header by specified name, or {@code null} if no header for the name.
     *
     * @param name header name
     * @return header value or {@code null} if no header for the name
     */
    public String get(String name) {
        return headers.get(name);
    }

    /**
     * remove header by name
     *
     * @param name header name
     * @return the previous header value associated with <tt>name</tt>, or
     *         <tt>null</tt> if there was no header for given <tt>name</tt>.
     */
    public String remove(String name) {
        return headers.remove(name);
    }

    /**
     * Returns <tt>true</tt> if this HttpHeaders contains a header for the specified name.
     *
     * @param name header name
     * @return <tt>true</tt> if this HttpHeaders contains a header for the specified name.
     */
    public boolean has(String name) {
        return headers.containsKey(name);
    }

    /**
     * Returns <tt>false</tt> if this HttpHeaders contains no header for the specified name.
     *
     * @param name header name
     * @return <tt>true</tt> if this HttpHeaders contains no header for the specified name.
     */
    public boolean hasNot(String name) {
        return !headers.containsKey(name);
    }

    /**
     * Returns <tt>true</tt> if this HttpHeaders contains a header for the specified name and header value is equal with given.
     *
     * @param name header name
     * @param value header value
     * @return <tt>true</tt> if this HttpHeaders contains a header for the specified name and header value is equal with given.
     */
    public boolean hasAndEqual(String name, String value) {
        final String header = headers.get(name);
        if (isNull(header)) {
            return false;
        }

        return header.equals(value);
    }

    /**
     * Returns the number of headers in this HttpHeaders
     *
     * @return the number of headers in this HttpHeaders
     */
    public int size() {
        return headers.size();
    }

    /**
     * Returns <tt>true</tt> if this HttpHeaders contains no headers.
     *
     * @return <tt>true</tt> if this HttpHeaders contains no headers
     */
    public boolean isEmpty() {
        return headers.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this HttpHeaders contains headers.
     *
     * @return <tt>true</tt> if this HttpHeaders contains headers
     */
    public boolean isNotEmpty() {
        return headers.size() > 0;
    }

    /**
     * Performs the given action for each header until all entries
     * have been processed or the action throws an exception.
     *
     * @param action The action to be performed for each header
     */
    public void forEach(BiConsumer<String, String> action) {
        headers.forEach(action);
    }

    /**
     * remove all headers
     */
    public void clear() {
        headers.clear();
    }

    /**
     * Returns copy of this HttpHeaders
     *
     * @return copy HttpHeaders
     */
    public HttpHeaders copy() {
        return new HttpHeaders(this);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        headers.forEach((name, value) -> {
            sb.append(name);
            sb.append(": ");
            sb.append(value);
            sb.append("\n");
        });

        return sb.toString().trim();
    }

    @Override
    public int hashCode() {
        return headers.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final HttpHeaders that = (HttpHeaders) o;

        return headers.equals(that.headers);
    }
}
