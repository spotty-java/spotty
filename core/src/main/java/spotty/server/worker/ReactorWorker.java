/*
 * Copyright 2022 - Alex Danilenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spotty.server.worker;

import com.google.common.annotations.VisibleForTesting;
import spotty.common.exception.SpottyException;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static spotty.common.utils.ThreadUtils.threadPool;
import static spotty.common.validation.Validation.notNull;

public final class ReactorWorker implements Closeable {
    private final ThreadPoolExecutor reactorPool;

    public ReactorWorker(int minWorkers, int maxWorkers, long keepAliveTime, TimeUnit timeUnit) {
        reactorPool = new ThreadPoolExecutor(
            minWorkers,
            maxWorkers,
            keepAliveTime,
            notNull("timeUnit", timeUnit),
            new SynchronousQueue<>(),
            threadPool("spotty-reactor"),
            new RejectedHandler()
        );
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

    @VisibleForTesting
    ThreadPoolExecutor reactorPool() {
        return reactorPool;
    }

    /**
     * When the reactor pool rejects a task due to insufficient space in the queue,
     * this handler inserts the specified element into the reactor pool queue
     * and waits for space to become available if needed.
     */
    private static class RejectedHandler implements RejectedExecutionHandler {

        private final ExecutorService rejectedHandlerExecutor = newSingleThreadExecutor();

        @Override
        public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
            rejectedHandlerExecutor.execute(() -> {
                try {
                    executor.getQueue().put(task);
                } catch (Exception e) {
                    throw new SpottyException(e);
                }
            });
        }

    }

}
