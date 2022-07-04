package spotty.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
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

    private volatile boolean running = false;
    private volatile boolean started = false;

    private final int port;
    private final int connections;
    private final ExecutorService CONNECTION_WORKERS;

    public Spotty() {
        this(DEFAULT_PORT, DEFAULT_CONNECTIONS);
    }

    public Spotty(int port) {
        this(port, DEFAULT_CONNECTIONS);
    }

    public Spotty(int port, int connections) {
        this.port = port;
        this.connections = connections;

        CONNECTION_WORKERS = new ThreadPoolExecutor(
            3,
            connections,
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
        try(final var server = new ServerSocket(port, connections)) {
            log.info("server has been started on port " + server.getLocalPort());
            run();
            started();

            while (running && !Thread.currentThread().isInterrupted()) {

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

}
