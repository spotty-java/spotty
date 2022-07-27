package spotty.common.exception;

import spotty.common.http.HttpStatus;

import static spotty.common.validation.Validation.notBlank;

public class SpottyHttpException extends SpottyException {
    public final HttpStatus status;

    public SpottyHttpException(HttpStatus status) {
        this(status, status.reasonPhrase);
    }

    public SpottyHttpException(HttpStatus status, String message, Object... args) {
        this(status, null, message, args);
    }

    public SpottyHttpException(HttpStatus status, Throwable cause, String message, Object... args) {
        super(notBlank("message", message), cause, args);

        this.status = status;
    }

}
