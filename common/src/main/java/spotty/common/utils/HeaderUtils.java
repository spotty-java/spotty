package spotty.common.utils;

import spotty.common.exception.SpottyException;
import spotty.common.exception.SpottyHttpException;
import spotty.common.http.HttpMethod;

import java.net.URI;
import java.net.URISyntaxException;

import static spotty.common.http.HttpHeaders.CONTENT_LENGTH;
import static spotty.common.http.HttpStatus.BAD_REQUEST;

public final class HeaderUtils {
    private HeaderUtils() {
    }

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
}
