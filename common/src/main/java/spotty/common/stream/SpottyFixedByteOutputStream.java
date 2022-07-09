package spotty.common.stream;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import static java.lang.Math.min;

public class SpottyFixedByteOutputStream extends OutputStream {

    private byte[] data;
    private volatile int size = 0;
    private volatile int limit;

    public SpottyFixedByteOutputStream(int capacity) {
        this.data = new byte[capacity];
        this.limit = capacity;
    }

    @Override
    public synchronized void write(int b) throws IndexOutOfBoundsException {
        ensureCapacity();

        data[size++] = (byte) b;
    }

    @Override
    public synchronized void write(byte @NotNull [] b) throws IndexOutOfBoundsException {
        write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte @NotNull [] b, int off, int len) throws IndexOutOfBoundsException {
        ensureCapacity();
        Objects.checkFromIndexSize(off, len, b.length);

        if (len > remaining()) {
            throw new IndexOutOfBoundsException("length is bigger than remaining");
        }

        System.arraycopy(b, off, data, size, len);
        size += len;
    }

    public synchronized void write(ByteBuffer buffer) {
        write(buffer, 0, buffer.remaining());
    }

    public synchronized void write(ByteBuffer buffer, int off, int len) {
        ensureCapacity();
        if (len > remaining()) {
            throw new BufferOverflowException();
        }

        if (off > 0) {
            buffer.position(buffer.position() + off);
        }

        buffer.get(data, size, len);
        size += len;
    }

    public boolean isFull() {
        return size == limit;
    }

    public synchronized byte[] toByteArray() {
        return Arrays.copyOf(data, size);
    }

    public int remaining() {
        return limit - size;
    }

    public int capacity() {
        return data.length;
    }

    public synchronized void capacity(int capacity) {
        if (capacity < 0) {
            throw new IndexOutOfBoundsException("capacity must be >= 0");
        }

        if (data.length != capacity) {
            byte[] d = new byte[capacity];
            System.arraycopy(data, 0, d, 0, min(size, capacity));
            this.data = d;
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

    public synchronized void reset() {
        this.size = 0;
        this.limit = data.length;
    }

    @Override
    public synchronized String toString() {
        return new String(data, 0, size);
    }

    private void ensureCapacity() {
        if (size >= limit) {
            throw new IndexOutOfBoundsException("limit");
        }
    }
}
