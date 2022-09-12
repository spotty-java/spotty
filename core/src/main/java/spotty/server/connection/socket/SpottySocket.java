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
package spotty.server.connection.socket;

import spotty.server.connection.Connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public interface SpottySocket extends Closeable {
    SelectionKey register(Selector selector, int ops, Connection connection) throws IOException;

    int read(ByteBuffer dst) throws IOException;

    int write(ByteBuffer src) throws IOException;

    SocketAddress getRemoteAddress() throws IOException;

    boolean isOpen();

    boolean readBufferHasRemaining();

    @Override
    void close();
}
