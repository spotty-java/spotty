package spotty.server.exception;

public class SpottyException extends RuntimeException {

    public SpottyException(String message) {
        super(message);
    }

    public SpottyException(Throwable cause) {
        super(cause);
    }

    public SpottyException(String message, Throwable cause) {
        super(message, cause);
    }

}
