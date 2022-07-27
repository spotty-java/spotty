package spotty.common.exception;

public class SpottyValidationException extends SpottyException {

    public SpottyValidationException(String message, Object... args) {
        super(message, args);
    }

    public SpottyValidationException(Throwable cause) {
        super(cause);
    }

    public SpottyValidationException(String message, Throwable cause, Object... args) {
        super(message, cause, args);
    }

}
