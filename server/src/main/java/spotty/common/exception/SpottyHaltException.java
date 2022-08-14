package spotty.common.exception;

import spotty.common.http.HttpStatus;

import static spotty.common.validation.Validation.notBlank;

public class SpottyHaltException extends SpottyException {
    public final HttpStatus status;

    public SpottyHaltException(HttpStatus status) {
        this(status, status.statusMessage);
    }

    public SpottyHaltException(HttpStatus status, String message, Object... args) {
        this(status, null, message, args);
    }

    public SpottyHaltException(HttpStatus status, Throwable cause, String message, Object... args) {
        super(notBlank("message", message), cause, args);

        this.status = status;
    }
}
