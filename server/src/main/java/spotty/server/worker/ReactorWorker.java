package spotty.server.worker;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static spotty.common.utils.ThreadUtils.threadPool;

public final class ReactorWorker implements Closeable {
    private static final int DEFAULT_WORKERS = 24;

    private final ExecutorService reactorPool;

    public ReactorWorker() {
        this(DEFAULT_WORKERS);
    }

    public ReactorWorker(int workers) {
        reactorPool = Executors.newFixedThreadPool(workers, threadPool("spotty-worker"));
    }

    public void addAction(Runnable action) {
        reactorPool.execute(action);
    }

    @Override
    public void close() {
        try {
            reactorPool.shutdownNow();
        } catch (Exception e) {
            // ignore
        }
    }

}
