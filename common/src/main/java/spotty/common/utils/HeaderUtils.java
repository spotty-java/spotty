package spotty.common.utils;

import org.apache.http.entity.ContentType;
import spotty.common.exception.SpottyHttpException;
import spotty.common.http.HttpMethod;

import static spotty.common.http.Headers.CONTENT_LENGTH;
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

    public static ContentType parseContentType(String contentType) {
        try {
            return ContentType.parse(contentType);
        } catch (Exception e) {
            return null;
        }
    }

    public static HttpMethod parseHttpMethod(String method) {
        final HttpMethod res = HttpMethod.resolve(method.toUpperCase());
        if (res == null) {
            throw new SpottyHttpException(BAD_REQUEST, "unsupported method " + method);
        }

        return res;
    }
}
