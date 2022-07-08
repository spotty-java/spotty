package spotty.server.worker;

import lombok.extern.slf4j.Slf4j;
import spotty.server.worker.action.ReactorAction;

import java.io.Closeable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class ReactorWorker implements Closeable {
    private final BlockingQueue<ReactorAction> requests = new LinkedBlockingQueue<>();
    private final ExecutorService CONSUMER = Executors.newSingleThreadExecutor();
    private final ExecutorService WORKERS = new ThreadPoolExecutor(
        3,
        24,
        60,
        SECONDS,
        new LinkedBlockingQueue<>()
    );

    public ReactorWorker() {
        CONSUMER.execute(this::pollingActions);
    }

    public void addAction(ReactorAction action) {
        requests.add(action);
    }

    @Override
    public void close() {
        try {
            WORKERS.shutdownNow();
            CONSUMER.shutdownNow();
            requests.clear();
        } catch (Exception e) {
            // ignore
        }
    }

    private void pollingActions() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                final var action = requests.take();
                WORKERS.execute(() -> callAction(action));
            }
        } catch (Exception e) {
            log.error("reactor polling error", e);
        }
    }

    private void callAction(ReactorAction action) {
        try {
            action.call();
        } catch (Exception e) {
            log.error("reactor handle action error", e);
        }
    }
}
