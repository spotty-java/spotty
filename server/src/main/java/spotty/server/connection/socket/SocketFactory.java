package spotty.server.connection.socket;

import spotty.common.exception.SpottyStreamException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.nio.channels.SocketChannel;

import static spotty.common.validation.Validation.notNull;

public final class SocketFactory {

    private SSLContext sslContext;

    public void enableSsl(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public SpottySocket createSocket(SocketChannel socketChannel) {
        notNull("socketChannel", socketChannel);
        if (socketChannel.isBlocking()) {
            throw new SpottyStreamException("SocketChannel must be non blocking");
        }

        if (sslContext == null) {
            return new TCPSocket(socketChannel);
        }

        final SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false);

        return new SSLSocket(socketChannel, sslEngine);
    }

}
