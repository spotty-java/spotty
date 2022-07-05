package spotty.server.request;

import lombok.extern.slf4j.Slf4j;
import spotty.server.exception.SpottyHttpException;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.http.HttpHeaders.CONTENT_LENGTH;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

@Slf4j
public class RequestValidator {

    public static void validate(SpottyRequest request) throws SpottyHttpException {
        if (request == null) {
            throw new SpottyHttpException(SC_BAD_REQUEST, "request is empty");
        }

        if (request.contentLength < 0) {
            throw new SpottyHttpException(SC_BAD_REQUEST, "invalid " + CONTENT_LENGTH);
        }

        if (request.contentType.isEmpty()) {
            log.warn("empty " + CONTENT_TYPE);
        }

        notEmpty(request.protocol, "protocol");
        notEmpty(request.scheme, "scheme");
        notEmpty(request.method, "method");
        notEmpty(request.path, "path");
    }

}
