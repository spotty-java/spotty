package spotty.server.connection.state;

public enum ConnectionProcessorState {
    READY_TO_READ,
    READING_REQUEST_HEAD_LINE,
    HEADERS_READY_TO_READ,
    READING_HEADERS,
    BODY_READY_TO_READ,
    READING_BODY,
    BODY_READY,
    REQUEST_READY,
    REQUEST_HANDLING,
    READY_TO_WRITE,
    RESPONSE_WRITING,
    RESPONSE_WRITE_COMPLETED,
    CLOSED
}
