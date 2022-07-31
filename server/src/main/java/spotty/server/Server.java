package spotty.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotty.server.connection.Connection;
import spotty.server.connection.ConnectionProcessor;
import spotty.server.handler.request.RequestHandler;
import spotty.server.registry.exception.ExceptionHandlerRegistry;
import spotty.server.worker.ReactorWorker;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static spotty.common.utils.ThreadUtils.threadPool;
import static spotty.common.validation.Validation.notNull;
import static spotty.server.connection.state.ConnectionProcessorState.CLOSED;
import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_READ;
import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_WRITE;
import static spotty.server.connection.state.ConnectionProcessorState.REQUEST_HANDLING;

public final class Server implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private final ExecutorService SERVER_RUN = Executors.newSingleThreadExecutor(threadPool("spotty-main", false));

    private volatile boolean running = false;
    private volatile boolean started = false;

    private final AtomicLong connections = new AtomicLong();

    private final ReactorWorker reactorWorker = new ReactorWorker();

    private final int port;
    private final RequestHandler requestHandler;
    private final ExceptionHandlerRegistry exceptionHandlerRegistry;

    public Server(int port, RequestHandler requestHandler, ExceptionHandlerRegistry exceptionHandlerRegistry) {
        this.port = port;
        this.requestHandler = notNull("requestHandler", requestHandler);
        this.exceptionHandlerRegistry = notNull("exceptionHandlerRegistry", exceptionHandlerRegistry);
    }

    public synchronized void start() {
        if (started) {
            LOG.warn("server has been started already");
            return;
        }

        SERVER_RUN.execute(this::serverInit);
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
        return port;
    }

    private void serverInit() {
        try (final ServerSocketChannel serverSocket = ServerSocketChannel.open();
             final Selector selector = Selector.open()) {
            // Binding this server on the port
            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false); // Make Server nonBlocking
            serverSocket.register(selector, OP_ACCEPT);

            LOG.info("server has been started on port " + port);

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
        ServerSocketChannel serverSocket = (ServerSocketChannel) acceptKey.channel();
        SocketChannel socket = serverSocket.accept();
        socket.configureBlocking(false);

        final SelectionKey key = socket.register(acceptKey.selector(), OP_READ);

        final ConnectionProcessor connectionProcessor = new ConnectionProcessor(socket, requestHandler, reactorWorker, exceptionHandlerRegistry);
        final Connection connection = new Connection(connectionProcessor);

        LOG.debug("{} accepted, count={}", connection, connections.incrementAndGet());

        key.attach(connection);

        connection.whenStateIs(READY_TO_WRITE, __ -> {
            key.interestOps(OP_WRITE);
            key.selector().wakeup();
        });

        connection.whenStateIs(READY_TO_READ, __ -> {
            key.interestOps(OP_READ);
            key.selector().wakeup();
        });

        connection.whenStateIs(REQUEST_HANDLING, __ -> {
            key.interestOps(SelectionKey.OP_CONNECT); // newer connect, make key is waiting ready to write
        });

        connection.whenStateIs(CLOSED, __ -> {
            LOG.debug("{} closed, count={}", connection, connections.decrementAndGet());
            key.cancel();
        });
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
