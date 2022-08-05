package spotty.server.connection.socket;

import spotty.server.connection.Connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public interface SpottySocket extends Closeable {
    SelectionKey register(Selector selector, int ops, Connection connection) throws IOException;

    int read(ByteBuffer dst) throws IOException;

    int write(ByteBuffer src) throws IOException;

    SocketAddress getRemoteAddress() throws IOException;

    boolean isOpen();

    boolean readBufferHasRemaining();

    @Override
    void close();
}
