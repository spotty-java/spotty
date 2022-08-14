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
