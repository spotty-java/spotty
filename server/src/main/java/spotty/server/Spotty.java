package spotty.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import spotty.server.connection.Connection;
import spotty.server.handler.RequestHandler;
import spotty.server.handler.RouterRequestHandler;
import spotty.server.router.SpottyRouter;
import spotty.server.router.route.Route;
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

import static spotty.server.connection.state.ConnectionProcessorState.CLOSED;
import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_READ;
import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_WRITE;

@Slf4j
public final class Spotty implements Closeable {
    private static final int DEFAULT_PORT = 4000;

    static {
        ReactorWorker.init();
    }

    private final ExecutorService SERVER_RUN = Executors.newSingleThreadExecutor();

    private volatile boolean running = false;
    private volatile boolean started = false;

    private final int port;
    private final AtomicLong connections = new AtomicLong();

    private final SpottyRouter router = new SpottyRouter();
    private final RequestHandler requestHandler = new RouterRequestHandler(router);

    public Spotty() {
        this(DEFAULT_PORT);
    }

    public Spotty(int port) {
        this.port = port;
    }

    public synchronized void start() {
        if (started) {
            log.warn("server has been started already");
            return;
        }

        SERVER_RUN.execute(this::serverInit);
    }

    @Override
    public synchronized void close() {
        stop();
        SERVER_RUN.shutdownNow();
    }

    public synchronized void awaitUntilStart() {
        try {
            while (!started)
                wait(1000);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public synchronized void awaitUntilStop() {
        try {
            while (started)
                wait(1000);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isStarted() {
        return started;
    }

    public void get(String path, Route route) {
        router.get(path, route);
    }

    public void post(String path, Route route) {
        router.post(path, route);
    }

    public void put(String path, Route route) {
        router.put(path, route);
    }

    public void patch(String path, Route route) {
        router.patch(path, route);
    }

    public void delete(String path, Route route) {
        router.delete(path, route);
    }

    public void head(String path, Route route) {
        router.head(path, route);
    }

    public void trace(String path, Route route) {
        router.trace(path, route);
    }

    public void connect(String path, Route route) {
        router.connect(path, route);
    }

    public void options(String path, Route route) {
        router.options(path, route);
    }

    @SneakyThrows
    private void serverInit() {
        try (final ServerSocketChannel serverSocket = ServerSocketChannel.open();
             final Selector selector = Selector.open()) {
            // Binding this server on the port
            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false); // Make Server nonBlocking
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            log.info("server has been started on port " + port);

            run();
            started();

            while (running && !Thread.currentThread().isInterrupted()) {
                selector.select();
                final Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    final SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid())
                        continue;

                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                        write(key);
                    } else {
                        log.info("unsupported key ops {}", key.readyOps());
                    }
                }
            }
        } catch (IOException e) {
            log.error("start server error", e);
        } finally {
            close();

            stopped();
            log.info("server has been stopped");
        }
    }

    private void accept(SelectionKey acceptKey) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) acceptKey.channel();
        SocketChannel socket = serverSocket.accept();
        socket.configureBlocking(false);

        final SelectionKey key = socket.register(acceptKey.selector(), SelectionKey.OP_READ);

        final Connection connection = new Connection(socket, requestHandler);
        log.info("{} accepted: {}", connection, connections.incrementAndGet());

        key.attach(connection);

        connection.whenStateIs(READY_TO_WRITE, __ -> {
            key.interestOps(SelectionKey.OP_WRITE);
            key.selector().wakeup();
        });

        connection.whenStateIs(READY_TO_READ, __ ->
            key.interestOps(SelectionKey.OP_READ)
        );

        connection.whenStateIs(CLOSED, __ -> {
            if (connection.isClosed()) {
                log.info("{} closed: {}", connection, connections.decrementAndGet());
                key.cancel();
            } else {
                log.error("got CLOSED change state event, but connection didn't closed");
            }
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
