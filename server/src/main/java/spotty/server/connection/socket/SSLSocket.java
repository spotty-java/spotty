package spotty.server.connection.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotty.common.exception.SpottyException;
import spotty.common.exception.SpottyStreamException;
import spotty.common.exception.SpottyValidationException;
import spotty.server.connection.Connection;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static javax.net.ssl.SSLEngineResult.Status.OK;
import static spotty.common.utils.IOUtils.bufferCopyRemaining;
import static spotty.common.validation.Validation.notNull;

public final class SSLSocket implements SpottySocket {
    private static final Logger LOG = LoggerFactory.getLogger(SSLSocket.class);
    private static final int BUFFER_SIZE = 2048;

    private final SocketChannel socketChannel;
    private final SSLEngine sslEngine;

    private ByteBuffer peerNetBuffer;
    private ByteBuffer peerAppBuffer;
    private ByteBuffer myNetBuffer;
    private ByteBuffer myAppBuffer;

    public SSLSocket(SocketChannel socketChannel, SSLEngine sslEngine) {
        this.socketChannel = notNull("socketChannel", socketChannel);
        if (socketChannel.isBlocking()) {
            throw new SpottyStreamException("SocketChannel must be non blocking");
        }

        this.sslEngine = notNull("sslEngine", sslEngine);
        if (sslEngine.getUseClientMode()) {
            throw new SpottyValidationException("sslEngine must be in server mode");
        }

        this.peerNetBuffer = createBuffer(BUFFER_SIZE);
        this.peerAppBuffer = createBuffer(BUFFER_SIZE);

        this.myNetBuffer = createBuffer(BUFFER_SIZE);
        this.myAppBuffer = createBuffer(BUFFER_SIZE);
    }

    @Override
    public SelectionKey register(Selector selector, int ops, Connection connection) throws IOException {
        handshake();

        if (peerNetBuffer.position() > 0) {
            // buffer has some data that shouldn't be consumed,
            // very likely it is request that we have to handle
            connection.markDataRemaining();
        }

        selector.wakeup(); // wakeup to prevent deadlock
        return socketChannel.register(selector, ops, connection);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (peerAppBuffer.position() > 0 && peerAppBuffer.hasRemaining()) {
            return bufferCopyRemaining(peerAppBuffer, dst);
        }

        peerAppBuffer.clear();

        Status status;
        int read = 0;
        do {
            if (socketChannel.read(peerNetBuffer) == -1) {
                return -1;
            }

            if (peerNetBuffer.position() == 0) {
                return 0;
            }

            peerNetBuffer.flip();

            status = unwrap(peerNetBuffer, peerAppBuffer);
            LOG.debug("read.status {}", status);

            switch (status) {
                case OK:
                    peerAppBuffer.flip();
                    read = bufferCopyRemaining(peerAppBuffer, dst);
                    break;
                case BUFFER_OVERFLOW:
                    peerAppBuffer = enlargeApplicationBuffer(peerAppBuffer);
                    break;
                case BUFFER_UNDERFLOW:
                    peerNetBuffer = handleBufferUnderflow(peerNetBuffer);
                    break;
                case CLOSED:
                    close();
                    return read;
            }

            peerNetBuffer.compact();

        } while (status != OK);

        return read;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int writeData = 0;
        while (src.hasRemaining()) {
            myNetBuffer.clear();
            myAppBuffer.clear();

            bufferCopyRemaining(src, myAppBuffer);

            myAppBuffer.flip();

            Status status;
            do {
                status = wrap(myAppBuffer, myNetBuffer);
                LOG.debug("write.status {}", status);
                switch (status) {
                    case OK:
                        myNetBuffer.flip();
                        while (myNetBuffer.hasRemaining()) {
                            writeData += socketChannel.write(myNetBuffer);
                        }
                        break;
                    case BUFFER_OVERFLOW:
                        myNetBuffer = enlargePacketBuffer(myNetBuffer);
                        break;
                    case BUFFER_UNDERFLOW:
                        myAppBuffer = handleBufferUnderflow(myAppBuffer);
                        break;
                    case CLOSED:
                        close();
                        return writeData;
                }
            } while (status != OK);
        }

        return writeData;
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
        return peerAppBuffer.hasRemaining();
    }

    @Override
    public void close() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            // ignore
        }

        try {
            sslEngine.closeOutbound();
            sslEngine.closeInbound();
        } catch (IOException e) {
            // ignore
        }
    }

    private void handshake() throws IOException {
        sslEngine.beginHandshake();

        peerNetBuffer.clear();
        myNetBuffer.clear();

        while (socketChannel.isOpen() && !sslEngine.isOutboundDone()) {
            final HandshakeStatus status = sslEngine.getHandshakeStatus();
            switch (status) {
                case NEED_UNWRAP:
                    needUnwrap();
                    break;
                case NEED_WRAP:
                    needWrap();
                    break;
                case NEED_TASK:
                    needTask();
                    break;
                case NOT_HANDSHAKING:
                case FINISHED:
                    LOG.debug("Handshake done ({})", status);
                    return;
                default:
                    throw new SpottyException("undefined handshake status %s", status);
            }
        }
    }

    private void needUnwrap() throws IOException {
        final int read = socketChannel.read(peerNetBuffer);
        if (read == -1) {
            close();
            return;
        }

        peerNetBuffer.flip();

        final Status status = unwrap(peerNetBuffer, peerAppBuffer);
        peerNetBuffer.compact();

        LOG.debug("needUnwrap.status {}", status);
        switch (status) {
            case BUFFER_OVERFLOW:
                peerAppBuffer = enlargeApplicationBuffer(peerAppBuffer);
                break;
            case BUFFER_UNDERFLOW:
                if (read > 0) {
                    peerNetBuffer = handleBufferUnderflow(peerNetBuffer);
                }
                break;
            case CLOSED:
                close();
                break;
        }
    }

    private void needWrap() throws IOException {
        try {
            final Status status = wrap(myAppBuffer, myNetBuffer);
            LOG.debug("needWrap.status {}", status);
            switch (status) {
                case OK:
                    myNetBuffer.flip();
                    while (myNetBuffer.hasRemaining()) {
                        socketChannel.write(myNetBuffer);
                    }
                    break;
                case BUFFER_OVERFLOW:
                    myNetBuffer = enlargePacketBuffer(myNetBuffer);
                    break;
                case BUFFER_UNDERFLOW:
                    throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
                case CLOSED:
                    close();
                    break;
            }
        } finally {
            myNetBuffer.clear();
        }
    }

    private void needTask() {
        Runnable task;
        while ((task = sslEngine.getDelegatedTask()) != null) {
            task.run();
        }
    }

    private Status wrap(ByteBuffer src, ByteBuffer dest) throws SSLException {
        try {
            return sslEngine.wrap(src, dest).getStatus();
        } catch (SSLException e) {
            close();
            throw e;
        }
    }

    private Status unwrap(ByteBuffer src, ByteBuffer dest) throws SSLException {
        try {
            return sslEngine.unwrap(src, dest).getStatus();
        } catch (SSLException e) {
            close();
            throw e;
        }
    }

    private ByteBuffer createBuffer(int size) {
        return ByteBuffer.allocate(size);
    }

    private ByteBuffer enlargePacketBuffer(ByteBuffer buffer) {
        return enlargeBuffer(buffer, sslEngine.getSession().getPacketBufferSize());
    }

    private ByteBuffer enlargeApplicationBuffer(ByteBuffer buffer) {
        return enlargeBuffer(buffer, sslEngine.getSession().getApplicationBufferSize());
    }

    private ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            return createBuffer(sessionProposedCapacity);
        }

        return createBuffer(buffer.capacity() << 1);
    }

    private ByteBuffer handleBufferUnderflow(ByteBuffer buffer) {
        if (sslEngine.getSession().getPacketBufferSize() <= buffer.capacity()) {
            return buffer;
        }

        final ByteBuffer replaceBuffer = enlargePacketBuffer(buffer);
        if (buffer.position() > 0) {
            buffer.flip();
        }

        replaceBuffer.put(buffer);
        if (replaceBuffer.position() > 0) {
            replaceBuffer.flip();
        }

        return replaceBuffer;
    }

}
