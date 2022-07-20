package spotty.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import spotty.common.filter.Filter;
import spotty.common.http.HttpMethod;
import spotty.server.connection.Connection;
import spotty.server.connection.ConnectionProcessor;
import spotty.server.handler.exception.ExceptionHandler;
import spotty.server.handler.exception.ExceptionHandlerService;
import spotty.server.handler.request.DefaultRequestHandler;
import spotty.server.handler.request.RequestHandler;
import spotty.server.router.SpottyRouter;
import spotty.server.router.route.Route;
import spotty.server.router.route.RouteGroup;
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

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static spotty.version.SpottyVersion.VERSION;
import static spotty.common.http.Headers.DATE;
import static spotty.common.http.Headers.SERVER;
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
    private final RequestHandler requestHandler = new DefaultRequestHandler(router);
    private final ExceptionHandlerService exceptionHandlerService = new ExceptionHandlerService();

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

        registerSpottyDefaultAfterFilter();
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

    public int port() {
        return port;
    }

    public void path(String pathTemplate, RouteGroup group) {
        router.path(pathTemplate, group);
    }

    public void before(Filter filter, Filter... filters) {
        router.before(filter, filters);
    }

    public void after(Filter filter, Filter... filters) {
        router.after(filter, filters);
    }

    public void before(String pathTemplate, Filter filter, Filter... filters) {
        router.before(pathTemplate, filter, filters);
    }

    public void after(String pathTemplate, Filter filter, Filter... filters) {
        router.after(pathTemplate, filter, filters);
    }

    public void before(String pathTemplate, HttpMethod method, Filter filter, Filter... filters) {
        router.before(pathTemplate, method, filter, filters);
    }

    public void after(String pathTemplate, HttpMethod method, Filter filter, Filter... filters) {
        router.after(pathTemplate, method, filter, filters);
    }

    public void before(String pathTemplate, HttpMethod method, String acceptType, Filter filter, Filter... filters) {
        router.before(pathTemplate, method, acceptType, filter, filters);
    }

    public void after(String pathTemplate, HttpMethod method, String acceptType, Filter filter, Filter... filters) {
        router.after(pathTemplate, method, acceptType, filter, filters);
    }

    public void get(String pathTemplate, Route route) {
        router.get(pathTemplate, route);
    }

    public void post(String pathTemplate, Route route) {
        router.post(pathTemplate, route);
    }

    public void put(String pathTemplate, Route route) {
        router.put(pathTemplate, route);
    }

    public void patch(String pathTemplate, Route route) {
        router.patch(pathTemplate, route);
    }

    public void delete(String pathTemplate, Route route) {
        router.delete(pathTemplate, route);
    }

    public void head(String pathTemplate, Route route) {
        router.head(pathTemplate, route);
    }

    public void trace(String pathTemplate, Route route) {
        router.trace(pathTemplate, route);
    }

    public void connect(String pathTemplate, Route route) {
        router.connect(pathTemplate, route);
    }

    public void options(String pathTemplate, Route route) {
        router.options(pathTemplate, route);
    }

    public void get(String pathTemplate, String acceptType, Route route) {
        router.get(pathTemplate, acceptType, route);
    }

    public void post(String pathTemplate, String acceptType, Route route) {
        router.post(pathTemplate, acceptType, route);
    }

    public void put(String pathTemplate, String acceptType, Route route) {
        router.put(pathTemplate, acceptType, route);
    }

    public void patch(String pathTemplate, String acceptType, Route route) {
        router.patch(pathTemplate, acceptType, route);
    }

    public void delete(String pathTemplate, String acceptType, Route route) {
        router.delete(pathTemplate, acceptType, route);
    }

    public void head(String pathTemplate, String acceptType, Route route) {
        router.head(pathTemplate, acceptType, route);
    }

    public void trace(String pathTemplate, String acceptType, Route route) {
        router.trace(pathTemplate, acceptType, route);
    }

    public void connect(String pathTemplate, String acceptType, Route route) {
        router.connect(pathTemplate, acceptType, route);
    }

    public void options(String pathTemplate, String acceptType, Route route) {
        router.options(pathTemplate, acceptType, route);
    }

    public void clearRoutes() {
        router.clearRoutes();
    }

    public boolean removeRoute(String pathTemplate) {
        return router.removeRoute(pathTemplate);
    }

    public boolean removeRoute(String pathTemplate, HttpMethod method) {
        return router.removeRoute(pathTemplate, method);
    }

    public boolean removeRoute(String pathTemplate, String acceptType, HttpMethod method) {
        return router.removeRoute(pathTemplate, acceptType, method);
    }

    public <T extends Exception> void exception(Class<T> exceptionClass, ExceptionHandler exceptionHandler) {
        exceptionHandlerService.register(exceptionClass, exceptionHandler);
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

        final ConnectionProcessor connectionProcessor = new ConnectionProcessor(socket, requestHandler, exceptionHandlerService);
        final Connection connection = new Connection(connectionProcessor);

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

    private static final String SPOTTY_VERSION = "Spotty v" + VERSION;
    private void registerSpottyDefaultAfterFilter() {
        after((request, response) -> {
            response.headers()
                .add(DATE, RFC_1123_DATE_TIME.format(now(UTC)))
                .add(SERVER, SPOTTY_VERSION)
            ;
        });
    }

}
