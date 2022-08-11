package spotty.server.worker;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static spotty.common.utils.ThreadUtils.threadPool;

public final class ReactorWorker implements Closeable {
    private final ExecutorService reactorPool;

    public ReactorWorker(int workers) {
        reactorPool = Executors.newFixedThreadPool(workers, threadPool("spotty-worker"));
    }

    public void addTask(Runnable task) {
        reactorPool.execute(task);
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
