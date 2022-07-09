package spotty.common.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class Headers {
    /** RFC 2616 (HTTP/1.1) Section 14.1 */
    public static final String ACCEPT = "accept";

    /** RFC 2616 (HTTP/1.1) Section 14.2 */
    public static final String ACCEPT_CHARSET = "accept-charset";

    /** RFC 2616 (HTTP/1.1) Section 14.3 */
    public static final String ACCEPT_ENCODING = "accept-encoding";

    /** RFC 2616 (HTTP/1.1) Section 14.4 */
    public static final String ACCEPT_LANGUAGE = "accept-language";

    /** RFC 2616 (HTTP/1.1) Section 14.5 */
    public static final String ACCEPT_RANGES = "accept-ranges";

    /** RFC 2616 (HTTP/1.1) Section 14.6 */
    public static final String AGE = "age";

    /** RFC 1945 (HTTP/1.0) Section 10.1, RFC 2616 (HTTP/1.1) Section 14.7 */
    public static final String ALLOW = "allow";

    /** RFC 1945 (HTTP/1.0) Section 10.2, RFC 2616 (HTTP/1.1) Section 14.8 */
    public static final String AUTHORIZATION = "authorization";

    /** RFC 2616 (HTTP/1.1) Section 14.9 */
    public static final String CACHE_CONTROL = "cache-control";

    /** RFC 2616 (HTTP/1.1) Section 14.10 */
    public static final String CONNECTION = "connection";

    /** RFC 1945 (HTTP/1.0) Section 10.3, RFC 2616 (HTTP/1.1) Section 14.11 */
    public static final String CONTENT_ENCODING = "content-encoding";

    /** RFC 2616 (HTTP/1.1) Section 14.12 */
    public static final String CONTENT_LANGUAGE = "content-language";

    /** RFC 1945 (HTTP/1.0) Section 10.4, RFC 2616 (HTTP/1.1) Section 14.13 */
    public static final String CONTENT_LENGTH = "content-length";

    /** RFC 2616 (HTTP/1.1) Section 14.14 */
    public static final String CONTENT_LOCATION = "content-location";

    /** RFC 2616 (HTTP/1.1) Section 14.15 */
    public static final String CONTENT_MD5 = "content-md5";

    /** RFC 2616 (HTTP/1.1) Section 14.16 */
    public static final String CONTENT_RANGE = "content-range";

    /** RFC 1945 (HTTP/1.0) Section 10.5, RFC 2616 (HTTP/1.1) Section 14.17 */
    public static final String CONTENT_TYPE = "content-type";

    /** RFC 1945 (HTTP/1.0) Section 10.6, RFC 2616 (HTTP/1.1) Section 14.18 */
    public static final String DATE = "date";

    /** RFC 2518 (WevDAV) Section 9.1 */
    public static final String DAV = "dav";

    /** RFC 2518 (WevDAV) Section 9.2 */
    public static final String DEPTH = "depth";

    /** RFC 2518 (WevDAV) Section 9.3 */
    public static final String DESTINATION = "destination";

    /** RFC 2616 (HTTP/1.1) Section 14.19 */
    public static final String ETAG = "etag";

    /** RFC 2616 (HTTP/1.1) Section 14.20 */
    public static final String EXPECT = "expect";

    /** RFC 1945 (HTTP/1.0) Section 10.7, RFC 2616 (HTTP/1.1) Section 14.21 */
    public static final String EXPIRES = "expires";

    /** RFC 1945 (HTTP/1.0) Section 10.8, RFC 2616 (HTTP/1.1) Section 14.22 */
    public static final String FROM = "from";

    /** RFC 2616 (HTTP/1.1) Section 14.23 */
    public static final String HOST = "host";

    /** RFC 2518 (WevDAV) Section 9.4 */
    public static final String IF = "if";

    /** RFC 2616 (HTTP/1.1) Section 14.24 */
    public static final String IF_MATCH = "if-match";

    /** RFC 1945 (HTTP/1.0) Section 10.9, RFC 2616 (HTTP/1.1) Section 14.25 */
    public static final String IF_MODIFIED_SINCE = "if-modified-since";

    /** RFC 2616 (HTTP/1.1) Section 14.26 */
    public static final String IF_NONE_MATCH = "if-none-match";

    /** RFC 2616 (HTTP/1.1) Section 14.27 */
    public static final String IF_RANGE = "if-range";

    /** RFC 2616 (HTTP/1.1) Section 14.28 */
    public static final String IF_UNMODIFIED_SINCE = "if-unmodified-since";

    /** RFC 1945 (HTTP/1.0) Section 10.10, RFC 2616 (HTTP/1.1) Section 14.29 */
    public static final String LAST_MODIFIED = "last-modified";

    /** RFC 1945 (HTTP/1.0) Section 10.11, RFC 2616 (HTTP/1.1) Section 14.30 */
    public static final String LOCATION = "location";

    /** RFC 2518 (WevDAV) Section 9.5 */
    public static final String LOCK_TOKEN = "lock-token";

    /** RFC 2616 (HTTP/1.1) Section 14.31 */
    public static final String MAX_FORWARDS = "max-forwards";

    /** RFC 2518 (WevDAV) Section 9.6 */
    public static final String OVERWRITE = "overwrite";

    /** RFC 1945 (HTTP/1.0) Section 10.12, RFC 2616 (HTTP/1.1) Section 14.32 */
    public static final String PRAGMA = "pragma";

    /** RFC 2616 (HTTP/1.1) Section 14.33 */
    public static final String PROXY_AUTHENTICATE = "proxy-authenticate";

    /** RFC 2616 (HTTP/1.1) Section 14.34 */
    public static final String PROXY_AUTHORIZATION = "proxy-authorization";

    /** RFC 2616 (HTTP/1.1) Section 14.35 */
    public static final String RANGE = "range";

    /** RFC 1945 (HTTP/1.0) Section 10.13, RFC 2616 (HTTP/1.1) Section 14.36 */
    public static final String REFERER = "referer";

    /** RFC 2616 (HTTP/1.1) Section 14.37 */
    public static final String RETRY_AFTER = "retry-after";

    /** RFC 1945 (HTTP/1.0) Section 10.14, RFC 2616 (HTTP/1.1) Section 14.38 */
    public static final String SERVER = "server";

    /** RFC 2518 (WevDAV) Section 9.7 */
    public static final String STATUS_URI = "status-uri";

    /** RFC 2616 (HTTP/1.1) Section 14.39 */
    public static final String TE = "te";

    /** RFC 2518 (WevDAV) Section 9.8 */
    public static final String TIMEOUT = "timeout";

    /** RFC 2616 (HTTP/1.1) Section 14.40 */
    public static final String TRAILER = "trailer";

    /** RFC 2616 (HTTP/1.1) Section 14.41 */
    public static final String TRANSFER_ENCODING = "transfer-encoding";

    /** RFC 2616 (HTTP/1.1) Section 14.42 */
    public static final String UPGRADE = "upgrade";

    /** RFC 1945 (HTTP/1.0) Section 10.15, RFC 2616 (HTTP/1.1) Section 14.43 */
    public static final String USER_AGENT = "user-agent";

    /** RFC 2616 (HTTP/1.1) Section 14.44 */
    public static final String VARY = "vary";

    /** RFC 2616 (HTTP/1.1) Section 14.45 */
    public static final String VIA = "via";

    /** RFC 2616 (HTTP/1.1) Section 14.46 */
    public static final String WARNING = "warning";

    /** RFC 1945 (HTTP/1.0) Section 10.16, RFC 2616 (HTTP/1.1) Section 14.47 */
    public static final String WWW_AUTHENTICATE = "www-authenticate";

    private final Map<String, String> headers = new HashMap<>();

    public Headers() {
    }

    public Headers(Headers headers) {
        this.headers.putAll(headers.headers);
    }

    public Headers add(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public Headers add(Headers headers) {
        this.headers.putAll(headers.headers);
        return this;
    }

    public String get(String name) {
        return headers.get(name);
    }

    public String remove(String name) {
        return headers.remove(name);
    }

    public boolean contain(String name) {
        return headers.containsKey(name);
    }

    public boolean notContain(String name) {
        return !headers.containsKey(name);
    }

    public int size() {
        return headers.size();
    }

    public boolean isEmpty() {
        return headers.size() == 0;
    }

    public boolean isNotEmpty() {
        return headers.size() > 0;
    }

    public void forEach(BiConsumer<String, String> consumer) {
        headers.forEach(consumer);
    }

    public void clear() {
        headers.clear();
    }

    public Headers copy() {
        return new Headers(this);
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();
        headers.forEach((name, value) -> {
            sb.append(name);
            sb.append(": ");
            sb.append(value);
            sb.append("\n");
        });

        sb.append("\n");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return headers.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Headers that = (Headers) o;

        return Objects.equals(headers, that.headers);
    }
}
