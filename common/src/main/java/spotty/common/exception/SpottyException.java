package spotty.common.exception;

import static java.lang.String.format;

public class SpottyException extends RuntimeException {

    public SpottyException(String message, Object... args) {
        super(format(message, args));
    }

    public SpottyException(Throwable cause) {
        super(cause);
    }

    public SpottyException(String message, Throwable cause, Object... args) {
        super(format(message, args), cause);
    }

}
