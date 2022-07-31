package spotty.common.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadUtils {

    public static ThreadFactory threadPool(String poolName) {
        return threadPool(poolName, true);
    }

    public static ThreadFactory threadPool(String poolName, boolean isDaemon) {
        final AtomicInteger threadNumber = new AtomicInteger();
        return runnable -> {
            final String name = String.format("%s-thread-%s", poolName, threadNumber.incrementAndGet());
            final Thread thread = new Thread(runnable, name);
            thread.setDaemon(isDaemon);

            return thread;
        };
    }
}
