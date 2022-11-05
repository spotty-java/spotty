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
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static spotty.common.utils.ThreadUtils.threadPool;
import static spotty.common.validation.Validation.isNotBlank;
import static spotty.common.validation.Validation.notBlank;
import static spotty.common.validation.Validation.notNull;
import static spotty.common.validation.Validation.validate;
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

    private final AtomicInteger connections = new AtomicInteger();

    private final SocketFactory socketFactory = new SocketFactory();

    private final int maxRequestBodySize;
    private final RequestHandler requestHandler;
    private final ExceptionHandlerRegistry exceptionHandlerRegistry;
    private final ReactorWorker reactorWorker ;
    private final InetSocketAddress socketAddress;

    public Server(int port, int maxRequestBodySize, RequestHandler requestHandler, ExceptionHandlerRegistry exceptionHandlerRegistry, ReactorWorker reactorWorker) {
        validate(maxRequestBodySize > 0, "maximum request body size must be greater then 0");

        this.maxRequestBodySize = maxRequestBodySize;
        this.requestHandler = notNull("requestHandler", requestHandler);
        this.exceptionHandlerRegistry = notNull("exceptionHandlerRegistry", exceptionHandlerRegistry);
        this.reactorWorker = notNull("reactorWorker", reactorWorker);
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
        try {
            // Initialise the keystore
            final KeyStore ks = KeyStore.getInstance("JKS");
            try (final InputStream file = new FileInputStream(keyStorePath)) {
                ks.load(file, keyPassword);
            }

            // Set up the key manager factory
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keyPassword);

            final KeyStore tks;
            if (isNotBlank(trustStorePath)) {
                // Initialise the keystore
                tks = KeyStore.getInstance("JKS");
                try (final InputStream file = new FileInputStream(trustStorePath)) {
                    final char[] trustPassword = trustStorePassword == null ? null : trustStorePassword.toCharArray();
                    tks.load(file, trustPassword);
                }
            } else {
                tks = ks;
            }

            // Set up the trust manager factory
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(tks);

            final TrustManager[] trustMapper = tmf.getTrustManagers();
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

    public int connections() {
        return connections.get();
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

            LOG.info("server has been started {}", hostUrl());

            run();
            started();

            final Thread currentThread = Thread.currentThread();
            while (running && !currentThread.isInterrupted()) {
                selector.select(1000);
                final Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    final SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        final Connection connection = (Connection) key.attachment();
                        connection.close();
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
        } catch (Exception e) {
            LOG.error("start server error", e);
        } finally {
            close();

            stopped();
            LOG.info("server has been stopped {}", hostUrl());
        }
    }

    private void accept(SelectionKey acceptKey) throws IOException {
        final ServerSocketChannel serverSocket = (ServerSocketChannel) acceptKey.channel();
        final SocketChannel channel = serverSocket.accept();
        channel.configureBlocking(false);

        final SpottySocket socket = socketFactory.createSocket(channel);

        final Connection connection = new Connection(socket, requestHandler, reactorWorker, exceptionHandlerRegistry, maxRequestBodySize);
        LOG.debug("{} accepted, count={}", connection, connections.incrementAndGet());

        connection.whenStateIs(CLOSED, () -> {
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
            connection.close();
            return;
        }

        LOG.debug("socket registered {}", connection);

        connection.whenStateIs(READY_TO_WRITE, () -> {
            key.interestOps(OP_WRITE);
            key.selector().wakeup();
        });

        connection.whenStateIs(READY_TO_READ, () -> {
            key.interestOps(OP_READ);
            key.selector().wakeup();
        });

        connection.whenStateIs(CLOSED, key::cancel);

        // mark connection ready to ready if it's initialized
        if (connection.is(INITIALIZED)) {
            connection.markReadyToRead();
        }

        // if after initialization (https handshake) socket has not processed data
        if (connection.is(DATA_REMAINING)) {
            connection.handle();
        }

        // if after initialization or more possible after DATA_REMAINING key in OP_CONNECT, change it to ready to read
        if (key.isValid() && key.interestOps() == OP_CONNECT && connection.isNot(REQUEST_HANDLING)) {
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
