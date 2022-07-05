package spotty.server.exception;

public class SpottyStreamException extends SpottyException {

    public SpottyStreamException(String message, Object... params) {
        super(String.format(message, params));
    }

}
