package spotty.common.exception;

public class SpottyValidationException extends SpottyException {

    public SpottyValidationException(String message, Object... params) {
        super(params.length > 0 ? String.format(message, params) : message);
    }

}
