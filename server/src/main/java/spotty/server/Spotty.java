package spotty.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import spotty.server.connection.Connection;
import spotty.server.connection.subscription.OnCloseSubscription;
import spotty.server.connection.subscription.OnMessageSubscription;
import spotty.server.exception.SpottyException;
import spotty.server.request.SpottyRequest;
import spotty.server.response.SpottyResponse;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.Math.max;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class Spotty implements Closeable {
    private static final int DEFAULT_PORT = 4000;
    private static final int DEFAULT_CONNECTIONS = max(10, Runtime.getRuntime().availableProcessors());

    private final ExecutorService SERVER_RUN = Executors.newSingleThreadExecutor();

    private final List<Connection> connections = new CopyOnWriteArrayList<>();

    private volatile boolean running = false;
    private volatile boolean started = false;

    private final int port;
    private final int maxConnections;
    private final ExecutorService CONNECTION_WORKERS;

    public Spotty() {
        this(DEFAULT_PORT, DEFAULT_CONNECTIONS);
    }

    public Spotty(int port) {
        this(port, DEFAULT_CONNECTIONS);
    }

    public Spotty(int port, int maxConnections) {
        this.port = port;
        this.maxConnections = maxConnections;

        CONNECTION_WORKERS = new ThreadPoolExecutor(
            3,
            maxConnections,
            60,
            SECONDS,
            new LinkedBlockingQueue<>()
        );
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
        if (!started) {
            log.warn("server isn't running now");
            return;
        }

        stop();
        SERVER_RUN.shutdownNow();
        CONNECTION_WORKERS.shutdownNow();
    }

    public synchronized void awaitUntilStart() {
        try {
            while (!started) {
                wait();
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public synchronized void awaitUntilStop() {
        try {
            while (started) {
                wait();
            }
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

    @SneakyThrows
    private void serverInit() {
        try (final var server = new ServerSocket(port, maxConnections)) {
            log.info("server has been started on port " + server.getLocalPort());
            run();
            started();

            final var subscriber = new SpottyOnCloseSubscription();
            while (running && !Thread.currentThread().isInterrupted()) {
                final var connection = new Connection(server.accept());
                connections.add(connection);

                connection.onCloseSubscribe(subscriber);
                connection.onMessageSubscribe(subscriber);

                CONNECTION_WORKERS.execute(connection::handle);
            }
        } catch (IOException e) {
            log.error("start server error", e);
        }

        log.info("server has been stopped");
        stopped();
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

    private class SpottyOnCloseSubscription implements OnCloseSubscription, OnMessageSubscription {
        @Override
        public void onClose(Connection connection) throws SpottyException {
            connections.remove(connection);
        }

        @Override
        public void onMessage(SpottyRequest request, SpottyResponse response) throws SpottyException {
            try {
                response.setBody(request.body.readAllBytes());
            } catch (IOException e) {
                throw new SpottyException(e);
            }
        }
    }

}
