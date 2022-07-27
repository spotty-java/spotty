package spotty.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotty.common.exception.SpottyHttpException;
import spotty.common.exception.SpottyValidationException;
import spotty.common.filter.Filter;
import spotty.common.http.HttpMethod;
import spotty.server.compress.Compressor;
import spotty.server.connection.Connection;
import spotty.server.connection.ConnectionProcessor;
import spotty.server.handler.exception.ExceptionHandler;
import spotty.server.handler.request.DefaultRequestHandler;
import spotty.server.registry.exception.ExceptionHandlerRegistry;
import spotty.server.router.SpottyRouter;
import spotty.server.router.route.Route;
import spotty.server.router.route.RouteGroup;
import spotty.server.session.SessionManager;
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
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static spotty.common.http.HttpHeaders.DATE;
import static spotty.common.http.HttpHeaders.SERVER;
import static spotty.common.http.HttpStatus.BAD_REQUEST;
import static spotty.common.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static spotty.common.utils.ThreadUtils.threadPool;
import static spotty.common.validation.Validation.isNotNull;
import static spotty.common.validation.Validation.notNull;
import static spotty.server.connection.state.ConnectionProcessorState.CLOSED;
import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_READ;
import static spotty.server.connection.state.ConnectionProcessorState.READY_TO_WRITE;
import static spotty.server.connection.state.ConnectionProcessorState.REQUEST_HANDLING;
import static spotty.version.SpottyVersion.VERSION;

public final class Spotty implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(Spotty.class);
    private static final String SPOTTY_VERSION = "Spotty v" + VERSION;
    private static final int DEFAULT_PORT = 4000;

    static {
        ReactorWorker.init();
    }

    private final ExecutorService SERVER_RUN = Executors.newSingleThreadExecutor(threadPool("spotty-main", false));

    private volatile boolean running = false;
    private volatile boolean started = false;

    private final int port;
    private final SessionManager sessionManager;
    private final DefaultRequestHandler requestHandler;

    private final AtomicLong connections = new AtomicLong();

    private final SpottyRouter router = new SpottyRouter();
    private final ExceptionHandlerRegistry exceptionHandlerRegistry = new ExceptionHandlerRegistry();

    public Spotty() {
        this(DEFAULT_PORT, SessionManager.builder().build());
    }

    public Spotty(int port) {
        this(port, SessionManager.builder().build());
    }

    public Spotty(int port, SessionManager sessionManager) {
        this.port = port;
        this.sessionManager = notNull("sessionManager", sessionManager);
        this.requestHandler = new DefaultRequestHandler(router, new Compressor(), sessionManager);
    }

    public synchronized void start() {
        if (started) {
            LOG.warn("server has been started already");
            return;
        }

        registerSpottyDefaultFilters();
        registerSpottyDefaultExceptionHandlers();

        SERVER_RUN.execute(this::serverInit);
    }

    public void enableSession() {
        sessionManager.enableSession();
    }

    @Override
    public synchronized void close() {
        stop();
        SERVER_RUN.shutdownNow();

        sessionManager.close();
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

    public <T extends Exception> void exception(Class<T> exceptionClass, ExceptionHandler<T> exceptionHandler) {
        exceptionHandlerRegistry.register(exceptionClass, exceptionHandler);
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

                    if (!key.isValid())
                        continue;

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

        final ConnectionProcessor connectionProcessor = new ConnectionProcessor(socket, requestHandler, exceptionHandlerRegistry);
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

    private void registerSpottyDefaultFilters() {
        before((request, response) -> {
            response.headers()
                .add(DATE, RFC_1123_DATE_TIME.format(now(UTC)))
                .add(SERVER, SPOTTY_VERSION)
            ;
        });
    }

    private void registerSpottyDefaultExceptionHandlers() {
        exception(SpottyHttpException.class, (exception, request, response) -> {
            response
                .status(exception.status)
                .body(exception.getMessage())
            ;
        });

        exception(SpottyValidationException.class, (exception, request, response) -> {
            response
                .status(BAD_REQUEST)
                .body(exception.getMessage())
            ;
        });

        exception(Exception.class, (exception, request, response) -> {
            response
                .status(INTERNAL_SERVER_ERROR)
                .body(INTERNAL_SERVER_ERROR.reasonPhrase)
            ;
        });
    }

}
