package spotty.server.connection;

import spotty.common.request.SpottyRequest;
import spotty.server.provider.SpottyNonBlockingRequestProvider;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public class ConnectionProcessor implements Closeable {
    public final UUID id = UUID.randomUUID();

    private final SocketChannel socketChannel;
    private final SpottyNonBlockingRequestProvider requestProvider;
    private ByteBuffer response;

    public ConnectionProcessor(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.requestProvider = new SpottyNonBlockingRequestProvider(socketChannel);
    }

    @Override
    public void close() {
        requestProvider.close();
    }

    public void read() throws IOException {
        requestProvider.read();
    }

    public void write() throws IOException {
        socketChannel.write(response);
    }

    public void setResponse(byte[] response) {
        this.response = ByteBuffer.wrap(response);
    }

    public void clearResponse() {
        this.response = null;
    }

    public void clearRequest() {
        requestProvider.resetRequest();
    }

    public boolean isWriteCompleted() {
        return response != null && !response.hasRemaining();
    }

    public boolean isMessageReady() {
        return requestProvider.isRequestReady();
    }

    public SpottyRequest request() {
        return requestProvider.request();
    }

    public boolean isClosed() {
        return !requestProvider.isOpen();
    }
}
