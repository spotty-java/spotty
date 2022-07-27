package spotty.common.exception;

import static spotty.common.http.HttpStatus.NOT_FOUND;

public class SpottyNotFoundException extends SpottyHttpException {
    public SpottyNotFoundException(String message, Object... args) {
        super(NOT_FOUND, message, args);
    }
}
