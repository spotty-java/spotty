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
package spotty.common.stream.output;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.lang.Math.min;

/**
 * OutputStream with fixed capacity, not thread safe, for single thread use only
 */
public class SpottyFixedByteOutputStream extends OutputStream {
    private static final byte LINE_SEPARATOR = '\n';

    private byte[] data;
    private int size = 0;
    private int limit;

    public SpottyFixedByteOutputStream(int capacity) {
        this.data = new byte[capacity];
        this.limit = capacity;
    }

    public void write(byte b) throws IndexOutOfBoundsException {
        ensureCapacity(1);

        data[size++] = b;
    }

    @Override
    public void write(int b) throws IndexOutOfBoundsException {
        write((byte) b);
    }

    @Override
    public void write(byte[] b) throws IndexOutOfBoundsException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IndexOutOfBoundsException {
        ensureCapacity(len);

        System.arraycopy(b, off, data, size, len);
        size += len;
    }

    public void print(String text) {
        write(text.getBytes());
    }

    public void println(String text) {
        print(text);
        println();
    }

    public void println() {
        newLine();
    }

    public void write(ByteBuffer buffer) {
        write(buffer, 0, buffer.remaining());
    }

    public void writeRemaining(ByteBuffer buffer) {
        write(buffer, 0, min(remaining(), buffer.remaining()));
    }

    public void write(ByteBuffer buffer, int off, int len) {
        ensureCapacity(len);

        if (off > 0) {
            buffer.position(buffer.position() + off);
        }

        buffer.get(data, size, len);
        size += len;
    }

    public boolean isFull() {
        return size == limit;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(data, size);
    }

    public int remaining() {
        return limit - size;
    }

    public int capacity() {
        return data.length;
    }

    public void capacity(int capacity) {
        if (capacity < 0) {
            throw new IndexOutOfBoundsException("capacity must be >= 0");
        }

        if (data.length != capacity) {
            byte[] d = new byte[capacity];
            this.size = min(size, capacity);

            if (size > 0) {
                System.arraycopy(data, 0, d, 0, size);
            }

            this.data = d;
            this.limit = capacity;
        }
    }

    public int limit() {
        return limit;
    }

    public void limit(int limit) {
        if (limit > capacity()) {
            throw new IndexOutOfBoundsException("limit can't be larger than capacity");
        }

        this.limit = limit;
    }

    public int size() {
        return size;
    }

    public void reset() {
        this.size = 0;
        this.limit = data.length;
    }

    @Override
    public String toString() {
        if (size > 0) {
            return new String(data, 0, size);
        }

        return "";
    }

    protected void ensureCapacity(int len) {
        if (len > remaining()) {
            throw new IndexOutOfBoundsException("not enough capacity size: " + size + " limit: " + limit);
        }
    }

    private void newLine() {
        write(LINE_SEPARATOR);
    }
}
