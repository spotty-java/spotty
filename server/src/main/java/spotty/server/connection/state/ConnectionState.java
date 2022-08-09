package spotty.server.connection.state;

import com.google.common.collect.Sets;

import java.util.Set;

public enum ConnectionState {
    DATA_REMAINING, // connection has some data that didn't consume and handle

    INITIALIZED,
    READY_TO_READ,
    READING_REQUEST_HEAD_LINE,
    HEADERS_READY_TO_READ,
    READING_HEADERS,
    PREPARE_HEADERS,
    BODY_READY_TO_READ,
    READING_BODY,
    BODY_READY,
    REQUEST_READY,
    REQUEST_HANDLING,
    READY_TO_WRITE,
    RESPONSE_WRITING,
    RESPONSE_WRITE_COMPLETED,
    CLOSED;

    private static final Set<ConnectionState> READING_STATES = Sets.newHashSet(
        INITIALIZED,
        READY_TO_READ,
        READING_REQUEST_HEAD_LINE,
        HEADERS_READY_TO_READ,
        READING_HEADERS,
        PREPARE_HEADERS,
        BODY_READY_TO_READ,
        READING_BODY,
        BODY_READY
    );

    public boolean isReading() {
        return READING_STATES.contains(this);
    }
}
