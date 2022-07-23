package spotty.common.exception;

import spotty.common.http.HttpStatus;

import static spotty.common.validation.Validation.notBlank;

public class SpottyHttpException extends SpottyException {
    public final HttpStatus status;

    public SpottyHttpException(HttpStatus status) {
        this(status, status.reasonPhrase);
    }

    public SpottyHttpException(HttpStatus status, String message) {
        this(status, message, null);
    }

    public SpottyHttpException(HttpStatus status, String message, Throwable cause) {
        super(notBlank("message", message), cause);

        this.status = status;
    }

}
