package spotty.common.test

enum TestState {
    DATA_REMAINING,
    READY_TO_READ,
    READY_TO_WRITE,
    RESPONSE_WRITE_COMPLETED,
    CLOSED
}