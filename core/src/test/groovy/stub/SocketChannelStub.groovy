package stub

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.channels.spi.SelectorProvider

import static java.lang.Math.min

class SocketChannelStub extends SocketChannel {
    private final ByteBuffer content

    SocketChannelStub(int size = 8192) {
        super(SelectorProvider.provider())

        this.content = ByteBuffer.allocate(size)
    }

    @Override
    SocketChannel bind(SocketAddress local) throws IOException {
        throw new UnsupportedOperationException()
    }

    @Override
    <T> SocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        throw new UnsupportedOperationException()
    }

    @Override
    SocketChannel shutdownInput() throws IOException {
        throw new UnsupportedOperationException()
    }

    @Override
    SocketChannel shutdownOutput() throws IOException {
        throw new UnsupportedOperationException()
    }

    @Override
    Socket socket() {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean isConnected() {
        return true
    }

    @Override
    boolean isConnectionPending() {
        return false
    }

    @Override
    boolean connect(SocketAddress remote) throws IOException {
        return true
    }

    @Override
    boolean finishConnect() throws IOException {
        return true
    }

    @Override
    SocketAddress getRemoteAddress() throws IOException {
        return new InetSocketAddress(InetAddress.getByName("localhost"), 3333)
    }

    @Override
    int read(ByteBuffer dst) throws IOException {
        if (!content.hasRemaining()) {
            return -1
        }

        int len = min(content.remaining(), dst.remaining())
        byte[] data = new byte[len]
        content.get(data)

        dst.put(data, 0, len)

        return len
    }

    @Override
    long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        throw new UnsupportedOperationException()
    }

    int write(String text) throws IOException {
        return write(ByteBuffer.wrap(text.getBytes()))
    }

    @Override
    int write(ByteBuffer src) throws IOException {
        final int len = min(content.remaining(), src.remaining())
        content.put(src)

        return len
    }

    byte[] getAllBytes() {
        byte[] data = new byte[content.remaining()]
        content.get(data)

        return data
    }

    void clear() {
        content.clear()
    }

    void flip() {
        content.flip()
    }

    boolean hasRemaining() {
        return content.hasRemaining()
    }

    @Override
    long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        throw new UnsupportedOperationException()
    }

    @Override
    SocketAddress getLocalAddress() throws IOException {
        throw new UnsupportedOperationException()
    }

    @Override
    <T> T getOption(SocketOption<T> name) throws IOException {
        throw new UnsupportedOperationException()
    }

    @Override
    Set<SocketOption<?>> supportedOptions() {
        throw new UnsupportedOperationException()
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {

    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {

    }
}
