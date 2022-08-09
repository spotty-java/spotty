package spotty.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotty.common.exception.SpottyException;
import spotty.server.connection.Connection;
import spotty.server.connection.socket.SocketFactory;
import spotty.server.connection.socket.SpottySocket;
import spotty.server.handler.request.RequestHandler;
import spotty.server.registry.exception.ExceptionHandlerRegistry;
import spotty.server.worker.ReactorWorker;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static spotty.common.utils.ThreadUtils.threadPool;
import static spotty.common.validation.Validation.isNotBlank;
import static spotty.common.validation.Validation.notBlank;
import static spotty.common.validation.Validation.notNull;
import static spotty.server.connection.state.ConnectionState.CLOSED;
import static spotty.server.connection.state.ConnectionState.DATA_REMAINING;
import static spotty.server.connection.state.ConnectionState.INITIALIZED;
import static spotty.server.connection.state.ConnectionState.READY_TO_READ;
import static spotty.server.connection.state.ConnectionState.READY_TO_WRITE;
import static spotty.server.connection.state.ConnectionState.REQUEST_HANDLING;

public final class Server implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private final ExecutorService SERVER_RUN = Executors.newSingleThreadExecutor(threadPool("spotty-main", false));

    private volatile boolean running = false;
    private volatile boolean started = false;
    private volatile boolean enabledHttps = false;

    private final AtomicLong connections = new AtomicLong();

    private final ReactorWorker reactorWorker = new ReactorWorker();
    private final SocketFactory socketFactory = new SocketFactory();

    private final int maxRequestBodySize;
    private final RequestHandler requestHandler;
    private final ExceptionHandlerRegistry exceptionHandlerRegistry;
    private final InetSocketAddress socketAddress;

    public Server(int port, int maxRequestBodySize, RequestHandler requestHandler, ExceptionHandlerRegistry exceptionHandlerRegistry) {
        this.maxRequestBodySize = maxRequestBodySize;
        this.requestHandler = notNull("requestHandler", requestHandler);
        this.exceptionHandlerRegistry = notNull("exceptionHandlerRegistry", exceptionHandlerRegistry);
        this.socketAddress = new InetSocketAddress(port);
    }

    public synchronized void start() {
        if (started) {
            LOG.warn("server has been started already");
            return;
        }

        SERVER_RUN.execute(this::serverInit);
    }

    public void enableHttps(String keyStorePath, String keyStorePassword, String trustStorePath, String trustStorePassword) {
        notBlank("keyStorePath", keyStorePath);

        final char[] keyPassword = keyStorePassword == null ? null : keyStorePassword.toCharArray();
        final char[] trustPassword = trustStorePassword == null ? null : trustStorePassword.toCharArray();

        try {
            // Initialise the keystore
            final KeyStore ks = KeyStore.getInstance("JKS");
            try (final InputStream file = new FileInputStream(keyStorePath)) {
                ks.load(file, keyPassword);
            }

            // Set up the key manager factory
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keyPassword);

            TrustManager[] trustMapper = null;
            if (isNotBlank(trustStorePath)) {
                // Initialise the keystore
                final KeyStore tks = KeyStore.getInstance("JKS");
                try (final InputStream file = new FileInputStream(trustStorePath)) {
                    tks.load(file, trustPassword);
                }

                // Set up the trust manager factory
                final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(tks);

                trustMapper = tmf.getTrustManagers();
            }

            final SSLContext sslContext = SSLContext.getInstance("SSL");

            // Set up the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), trustMapper, null);

            socketFactory.enableSsl(sslContext);
            enabledHttps = true;
        } catch (Exception e) {
            throw new SpottyException("ssl initialization error", e);
        }
    }

    @Override
    public synchronized void close() {
        stop();
        SERVER_RUN.shutdownNow();
        reactorWorker.close();
    }

    public synchronized void awaitUntilStart() {
        try {
            while (!started)
                wait(1000);
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    public synchronized void awaitUntilStop() {
        try {
            while (started)
                wait(1000);
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isStarted() {
        return started;
    }

    public int port() {
        return socketAddress.getPort();
    }

    public String host() {
        return socketAddress.getHostString();
    }

    public String hostUrl() {
        final StringBuilder sb = new StringBuilder("http");
        if (enabledHttps) {
            sb.append("s");
        }

        sb.append("://");
        sb.append(host());
        sb.append(":");
        sb.append(port());

        return sb.toString();
    }

    private void serverInit() {
        try (final ServerSocketChannel serverSocket = ServerSocketChannel.open();
             final Selector selector = Selector.open()) {
            // Binding this server on the port
            serverSocket.bind(socketAddress);
            serverSocket.configureBlocking(false); // Make Server nonBlocking
            serverSocket.register(selector, OP_ACCEPT);

            LOG.info("server has been started on port {}", socketAddress.getPort());

            run();
            started();

            while (running && !Thread.currentThread().isInterrupted()) {
                selector.select();
                final Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    final SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        key.cancel();
                        continue;
                    }

                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                        write(key);
                    } else {
                        LOG.warn("unsupported key ops {}", key.readyOps());
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("start server error", e);
        } finally {
            close();

            stopped();
            LOG.info("server has been stopped");
        }
    }

    private void accept(SelectionKey acceptKey) throws IOException {
        final ServerSocketChannel serverSocket = (ServerSocketChannel) acceptKey.channel();
        final SocketChannel channel = serverSocket.accept();
        channel.configureBlocking(false);

        final SpottySocket socket = socketFactory.createSocket(channel);

        final Connection connection = new Connection(socket, requestHandler, reactorWorker, exceptionHandlerRegistry, maxRequestBodySize);
        LOG.debug("{} accepted, count={}", connection, connections.incrementAndGet());

        connection.whenStateIs(CLOSED, __ -> {
            LOG.debug("{} closed, count={}", connection, connections.decrementAndGet());
        });

        if (enabledHttps) {
            reactorWorker.addTask(() -> registerConnection(connection, acceptKey.selector()));
        } else {
            registerConnection(connection, acceptKey.selector());
        }
    }

    private void registerConnection(Connection connection, Selector selector) {
        final SelectionKey key = connection.register(selector);
        if (key == null) {
            return;
        }

        LOG.debug("socket registered {}", connection);

        connection.whenStateIs(READY_TO_WRITE, __ -> {
            key.interestOps(OP_WRITE);
            key.selector().wakeup();
        });

        connection.whenStateIs(READY_TO_READ, __ -> {
            key.interestOps(OP_READ);
            key.selector().wakeup();
        });

        connection.whenStateIs(REQUEST_HANDLING, __ -> {
            key.interestOps(OP_CONNECT); // newer connect, make key is waiting ready to write
        });

        connection.whenStateIs(CLOSED, __ -> {
            key.cancel();
        });

        if (connection.is(INITIALIZED)) {
            connection.markReadyToRead();
        }

        if (connection.is(DATA_REMAINING)) {
            connection.handle();
        }

        if (key.isValid() && key.interestOps() == OP_CONNECT) {
            key.interestOps(OP_READ);
            key.selector().wakeup();
        }
    }

    private void read(SelectionKey key) {
        final Connection connection = (Connection) key.attachment();
        connection.handle();
    }

    private void write(SelectionKey key) {
        final Connection connection = (Connection) key.attachment();
        connection.handle();
    }

    private void run() {
        this.running = true;
    }

    private void stop() {
        this.running = false;
    }

    private synchronized void started() {
        this.started = true;
        notifyAll();
    }

    private synchronized void stopped() {
        this.started = false;
        notifyAll();
    }

}
