package spotty.common.exception;

import static java.lang.String.format;

public class SpottyStreamException extends SpottyException {
    public SpottyStreamException(String message, Object... params) {
        super(format(message, params));
    }
}
