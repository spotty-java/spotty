package spotty.common.exception;

public class SpottyException extends RuntimeException {

    public SpottyException(String message, Object... args) {
        super(args.length > 0 ? String.format(message, args) : message);
    }

    public SpottyException(Throwable cause) {
        super(cause);
    }

    public SpottyException(String message, Throwable cause, Object... args) {
        super(args.length > 0 ? String.format(message, args) : message, cause);
    }

}
