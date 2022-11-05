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
package spotty.common.stream.input;

import spotty.common.exception.SpottyStreamException;

import java.io.InputStream;
import java.nio.ByteBuffer;

public final class SpottyInputStream extends InputStream {
    private final byte[] data;

    private volatile int writeSequence = 0;
    private volatile int readSequence = 0;

    private volatile boolean writeLock = false;
    private volatile boolean readLock = false;
    private volatile boolean writeCompleted = false;
    private volatile boolean closed = false;

    private volatile int limit = Integer.MAX_VALUE;

    public SpottyInputStream() {
        this(2048);
    }

    public SpottyInputStream(int bufferSize) {
        this.data = new byte[bufferSize];
    }

    public int limit() {
        return limit;
    }

    public void limit(int limit) {
        this.limit = limit;
    }

    public int remaining() {
        return limit - writeSequence;
    }

    public boolean hasRemaining() {
        return remaining() > 0;
    }

    public int write(ByteBuffer b) {
        checkClosed();
        if (writeLock || writeCompleted || !hasRemaining()) {
            return 0;
        }

        try {
            writeLock = true;

            final int start = writeSequence;
            while (isNotFull() && b.hasRemaining() && hasRemaining()) {
                atomicWrite(b.get());
            }

            return writeSequence - start;
        } finally {
            if (!hasRemaining()) {
                writeCompleted();
            }

            writeLock = false;
            signalUnlock();
        }
    }

    @Override
    public int read() {
        checkClosed();
        checkReadLock();

        try {
            readLock = true;
            if (writeCompleted && isEmpty()) {
                return -1;
            }

            awaitData();
            return atomicRead();
        } finally {
            readLock = false;
        }
    }

    @Override
    public int read(byte[] b) {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) {
        checkClosed();
        checkReadLock();

        try {
            readLock = true;
            if (writeCompleted && isEmpty()) {
                return -1;
            }

            awaitData();

            int read = 0;
            int index = off;
            while (isNotEmpty() && read < len) {
                b[index++] = atomicRead();
                read++;
            }

            return read;
        } finally {
            readLock = false;
        }
    }

    public void writeCompleted() {
        writeCompleted = true;
        signalUnlock();
    }

    public boolean isWriteCompleted() {
        return writeCompleted;
    }

    @Override
    public void close() {
        closed = true;
    }

    public void clear() {
        writeSequence = 0;
        readSequence = 0;

        writeLock = false;
        readLock = false;
        writeCompleted = false;

        limit = Integer.MAX_VALUE;
    }

    private void atomicWrite(byte b) {
        final int nextWriteSeq = writeSequence + 1;
        data[nextWriteSeq % data.length] = b;
        writeSequence = nextWriteSeq;
    }

    private byte atomicRead() {
        final int nextReadSeq = readSequence + 1;
        final byte b = data[nextReadSeq % data.length];
        readSequence = nextReadSeq;

        return b;
    }

    private int size() {
        return writeSequence - readSequence;
    }

    private boolean isEmpty() {
        return readSequence >= writeSequence;
    }

    private boolean isFull() {
        return size() >= data.length;
    }

    private boolean isNotEmpty() {
        return !isEmpty();
    }

    private boolean isNotFull() {
        return !isFull();
    }

    private void checkReadLock() {
        if (readLock) {
            throw new SpottyStreamException("only only thread can read");
        }
    }

    private void checkClosed() {
        if (closed) {
            throw new SpottyStreamException("stream has been closed");
        }
    }

    private void awaitData() {
        if (isEmpty() && !writeCompleted) {
            synchronized (data) {
                try {
                    while (isEmpty() && !writeCompleted) {
                        data.wait(100);
                    }
                } catch (Exception e) {
                    throw new SpottyStreamException(e);
                }
            }
        }
    }

    private void signalUnlock() {
        synchronized (data) {
            data.notifyAll();
        }
    }
}
