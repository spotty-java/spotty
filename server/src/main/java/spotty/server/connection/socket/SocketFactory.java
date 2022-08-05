package spotty.server.connection.socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.nio.channels.SocketChannel;

public final class SocketFactory {

    private SSLContext sslContext;

    public void enableSsl(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public SpottySocket createSocket(SocketChannel socketChannel) {
        if (sslContext == null) {
            return new TCPSocket(socketChannel);
        }

        final SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false);

        return new SSLSocket(socketChannel, sslEngine);
    }

}
