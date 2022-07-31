package spotty.common.exception;

public class SpottySessionException extends SpottyException {

    public SpottySessionException(String message, Object... args) {
        super(message, args);
    }

    public SpottySessionException(Throwable cause) {
        super(cause);
    }

    public SpottySessionException(String message, Throwable cause, Object... args) {
        super(message, cause, args);
    }

}
