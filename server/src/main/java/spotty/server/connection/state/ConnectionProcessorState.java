package spotty.server.connection.state;

import spotty.common.state.State;

public enum ConnectionProcessorState implements State<ConnectionProcessorState> {
    READY_TO_READ(null),
    READING_HEADERS(READY_TO_READ),
    HEADERS_READY(READING_HEADERS),
    READING_BODY(HEADERS_READY),
    BODY_READY(READING_BODY),
    READY_TO_HANDLE_REQUEST(BODY_READY),
    REQUEST_HANDLING(READY_TO_HANDLE_REQUEST),
    RESPONSE_READY(REQUEST_HANDLING),
    READY_TO_WRITE(RESPONSE_READY),
    RESPONSE_WRITING(READY_TO_WRITE),
    RESPONSE_WRITE_COMPLETED(RESPONSE_WRITING),
    CLOSED(null);

    private final ConnectionProcessorState dependsOn;

    ConnectionProcessorState(ConnectionProcessorState dependsOn) {
        this.dependsOn = dependsOn;
    }

    @Override
    public ConnectionProcessorState dependsOn() {
        return dependsOn;
    }
}
