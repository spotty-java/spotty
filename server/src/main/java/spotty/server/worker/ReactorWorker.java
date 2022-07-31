package spotty.server.worker;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;
import static spotty.common.utils.ThreadUtils.threadPool;

public final class ReactorWorker implements Closeable {
    private static final int DEFAULT_MIN_WORKERS = 3;
    private static final int DEFAULT_MAX_WORKERS = 24;
    private static final int DEFAULT_WORKER_KEEP_ALIVE = 60;

    private final ExecutorService REACTOR_POOL;

    public ReactorWorker() {
        this(DEFAULT_MIN_WORKERS, DEFAULT_MAX_WORKERS, DEFAULT_WORKER_KEEP_ALIVE);
    }

    public ReactorWorker(int maxWorkers) {
        this(DEFAULT_MIN_WORKERS, maxWorkers, DEFAULT_WORKER_KEEP_ALIVE);
    }

    public ReactorWorker(int minWorkers, int maxWorkers) {
        this(minWorkers, maxWorkers, DEFAULT_WORKER_KEEP_ALIVE);
    }

    public ReactorWorker(int minWorkers, int maxWorkers, int keepAlive) {
        REACTOR_POOL = new ThreadPoolExecutor(
            minWorkers,
            maxWorkers,
            keepAlive,
            SECONDS,
            new LinkedBlockingQueue<>(),
            threadPool("spotty-worker")
        );
    }

    public void addAction(Runnable action) {
        REACTOR_POOL.execute(action);
    }

    @Override
    public void close() {
        try {
            REACTOR_POOL.shutdownNow();
        } catch (Exception e) {
            // ignore
        }
    }

}
