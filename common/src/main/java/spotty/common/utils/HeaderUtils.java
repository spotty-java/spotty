package spotty.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import spotty.common.exception.SpottyException;
import spotty.common.exception.SpottyHttpException;
import spotty.common.http.HttpMethod;

import java.net.URI;
import java.net.URISyntaxException;

import static spotty.common.http.HttpHeaders.CONTENT_LENGTH;
import static spotty.common.http.HttpStatus.BAD_REQUEST;

@Slf4j
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
            log.warn("invalid content type {}", contentType, e);
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

    public static URI parseUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new SpottyException("invalid uri syntax %s", e, uri);
        }
    }
}
