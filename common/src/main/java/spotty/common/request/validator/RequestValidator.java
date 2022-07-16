package spotty.common.request.validator;

import lombok.extern.slf4j.Slf4j;
import spotty.common.exception.SpottyHttpException;
import spotty.common.request.SpottyRequest;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static spotty.common.http.Headers.CONTENT_LENGTH;
import static spotty.common.http.Headers.CONTENT_TYPE;
import static spotty.common.http.HttpStatus.BAD_REQUEST;

@Slf4j
public final class RequestValidator {

    public static void validate(SpottyRequest request) throws SpottyHttpException {
        if (request == null) {
            throw new SpottyHttpException(BAD_REQUEST, "request is empty");
        }

        if (request.contentLength() < 0) {
            throw new SpottyHttpException(BAD_REQUEST, "invalid " + CONTENT_LENGTH);
        }

        if (request.contentType() == null) {
            log.warn("empty " + CONTENT_TYPE);
        }

        if (isBlank(request.protocol())) {
            throw new SpottyHttpException(BAD_REQUEST, "protocol is empty");
        }

        if (isBlank(request.scheme())) {
            throw new SpottyHttpException(BAD_REQUEST, "scheme is empty");
        }

        if (request.method() == null) {
            throw new SpottyHttpException(BAD_REQUEST, "method is empty");
        }

        if (request.path() == null) {
            throw new SpottyHttpException(BAD_REQUEST, "path is empty");
        }
    }

}
