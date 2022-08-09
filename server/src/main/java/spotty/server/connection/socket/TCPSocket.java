package spotty.server.connection.socket;

import spotty.server.connection.Connection;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static spotty.common.validation.Validation.notNull;

public final class TCPSocket implements SpottySocket {

    private final SocketChannel socketChannel;

    TCPSocket(SocketChannel socketChannel) {
        this.socketChannel = notNull("socketChannel", socketChannel);
    }

    @Override
    public SelectionKey register(Selector selector, int ops, Connection connection) throws IOException {
        return socketChannel.register(selector, ops, connection);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return socketChannel.read(dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return socketChannel.write(src);
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return socketChannel.getRemoteAddress();
    }

    @Override
    public boolean isOpen() {
        return socketChannel.isOpen();
    }

    @Override
    public boolean readBufferHasRemaining() {
        return false;
    }

    @Override
    public void close() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
