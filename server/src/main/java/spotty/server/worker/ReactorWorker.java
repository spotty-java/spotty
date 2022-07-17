package spotty.server.worker;

import lombok.extern.slf4j.Slf4j;
import spotty.common.exception.SpottyException;
import spotty.server.worker.action.ReactorAction;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public final class ReactorWorker implements Closeable {
    private static final int DEFAULT_MIN_WORKERS = 3;
    private static final int DEFAULT_MAX_WORKERS = 24;
    private static final int DEFAULT_WORKER_KEEP_ALIVE = 60;

    private static volatile ReactorWorker INSTANCE;

    private final ExecutorService WORKERS;

    public static void init() {
        init(DEFAULT_MAX_WORKERS);
    }

    public static void init(int maxWorkers) {
        init(DEFAULT_MIN_WORKERS, maxWorkers);
    }

    public static void init(int minWorkers, int maxWorkers) {
        init(minWorkers, maxWorkers, DEFAULT_WORKER_KEEP_ALIVE);
    }

    public synchronized static void init(int minWorkers, int maxWorkers, int keepAlive) {
        if (INSTANCE == null) {
            INSTANCE = new ReactorWorker(minWorkers, maxWorkers, keepAlive);
        }
    }

    public static ReactorWorker instance() {
        if (INSTANCE == null) {
            init();
        }

        return INSTANCE;
    }

    private ReactorWorker(int minWorkers, int maxWorkers, int keepAlive) {
        WORKERS = new ThreadPoolExecutor(
            minWorkers,
            maxWorkers,
            keepAlive,
            SECONDS,
            new LinkedBlockingQueue<>()
        );
    }

    public CompletableFuture<Void> addAction(ReactorAction action) {
        return CompletableFuture.runAsync(() -> callAction(action), WORKERS);
    }

    @Override
    public void close() {
        try {
            WORKERS.shutdownNow();
        } catch (Exception e) {
            // ignore
        }
    }

    private void callAction(ReactorAction action) {
        try {
            action.call();
        } catch (Exception e) {
            if (e instanceof SpottyException) {
                throw (SpottyException) e;
            }

            throw new SpottyException(e);
        }
    }
}
