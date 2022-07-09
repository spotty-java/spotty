package spotty.server.connection;

import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

public class Connection implements Closeable {
    private static final AtomicLong ID_GENERATOR = new AtomicLong();

    public final long id = ID_GENERATOR.incrementAndGet();

    private final ConnectionProcessor connectionProcessor;

    public Connection(SocketChannel socketChannel) {
        this.connectionProcessor = new ConnectionProcessor(socketChannel);
    }

    @Override
    public void close() {
        connectionProcessor.close();
    }

    public void read() throws IOException {
        connectionProcessor.read();
    }

    public void write() throws IOException {
        connectionProcessor.write();
    }

    public void prepareToWrite() {
        connectionProcessor.prepareToWrite();
    }

    public void resetResponse() {
        connectionProcessor.resetResponse();
    }

    public void resetRequest() {
        connectionProcessor.resetRequest();
    }

    public void requestHandlingState() {
        connectionProcessor.requestHandlingState();
    }

    public void readyToReadState() {
        connectionProcessor.readyToReadState();
    }

    public void responseReadyState() {
        connectionProcessor.responseReadyState();
    }

    public boolean isWriteCompleted() {
        return connectionProcessor.isWriteCompleted();
//        return writeBuffer != null && !writeBuffer.hasRemaining();
    }

    public boolean isReadyToHandleRequest() {
        return connectionProcessor.isReadyToHandleRequest();
    }

    public SpottyResponse response() {
        return connectionProcessor.response();
    }

    public SpottyRequest request() {
        return connectionProcessor.request();
    }

    public boolean isClosed() {
        return !connectionProcessor.isOpen();
    }
}
