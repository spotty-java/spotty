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

import java.io.Closeable;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static spotty.common.utils.ThreadUtils.threadPool;

public final class ReactorWorker implements Closeable {
    private final ExecutorService reactorPool;

    public ReactorWorker(int workers) {
        reactorPool = newFixedThreadPool(workers, threadPool("spotty-worker"));
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
