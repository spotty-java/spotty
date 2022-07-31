package spotty.common.utils;

import spotty.common.exception.SpottyException;
import spotty.common.exception.SpottyHttpException;
import spotty.common.http.HttpMethod;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static spotty.common.http.HttpHeaders.CONTENT_LENGTH;
import static spotty.common.http.HttpStatus.BAD_REQUEST;

public final class HeaderUtils {

    public static int parseContentLength(String contentLength) {
        try {
            return Integer.parseInt(contentLength);
        } catch (NumberFormatException e) {
            throw new SpottyHttpException(BAD_REQUEST, "invalid " + CONTENT_LENGTH);
        }
    }

    public static HttpMethod parseHttpMethod(String method) {
        final HttpMethod res = HttpMethod.resolve(method.toUpperCase());
        if (res == null) {
            throw new SpottyHttpException(BAD_REQUEST, "unsupported method " + method);
        }

        return res;
    }

    public static URI parseUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new SpottyException("invalid uri syntax %s", e, uri);
        }
    }

    public static Map<String, String> parseCookies(String cookiesString) {
        final Map<String, String> cookies = new HashMap<>();
        for (String cookie : cookiesString.split(";")) {
            final String[] parts = cookie.split("=");
            final String name = parts[0].trim();
            final String value = parts.length == 2 ? parts[1].trim() : "";

            cookies.put(name, value);
        }

        return unmodifiableMap(cookies);
    }
}
