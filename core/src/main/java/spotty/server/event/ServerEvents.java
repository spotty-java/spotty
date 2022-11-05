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
package spotty.server.event;

import java.nio.channels.SelectionKey;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public final class ServerEvents {
    private final Queue<SelectionKey> queue = new LinkedList<>();

    public void add(SelectionKey key) {
        queue.add(key);
    }

    public void add(Collection<SelectionKey> keys) {
        queue.addAll(keys);
    }

    public SelectionKey poll() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}