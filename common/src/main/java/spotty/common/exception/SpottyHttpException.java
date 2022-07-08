package spotty.common.exception;

import spotty.common.http.HttpStatus;

public class SpottyHttpException extends SpottyException {
    public final HttpStatus status;

    public SpottyHttpException(HttpStatus status, String message) {
        this(status, message, null);
    }

    public SpottyHttpException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);

        this.status = status;
    }

}
