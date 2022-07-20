package spotty.server.connection;

import spotty.server.connection.state.ConnectionProcessorState;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static org.apache.commons.lang3.Validate.notNull;

public final class Connection implements Closeable {
    private static final AtomicLong ID_GENERATOR = new AtomicLong();

    public final long id = ID_GENERATOR.incrementAndGet();

    private final ConnectionProcessor connectionProcessor;

    public Connection(ConnectionProcessor connectionProcessor) {
        this.connectionProcessor = notNull(connectionProcessor, "connectionProcessor");
    }

    @Override
    public void close() {
        connectionProcessor.close();
    }

    public void handle() {
        connectionProcessor.handle();
    }

    public boolean isClosed() {
        return connectionProcessor.isClosed();
    }

    public void whenStateIs(ConnectionProcessorState state, Consumer<ConnectionProcessorState> subscriber) {
        connectionProcessor.whenStateIs(state, subscriber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + "]";
    }
}
