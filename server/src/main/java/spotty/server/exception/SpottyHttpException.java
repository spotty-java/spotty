package spotty.server.exception;

public class SpottyHttpException extends SpottyException {
    public final int status;

    public SpottyHttpException(int status, String message) {
        this(status, message, null);
    }

    public SpottyHttpException(int status, String message, Throwable cause) {
        super(message, cause);

        this.status = status;
    }

}
