package spotty.common.stream.output;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class SpottyByteArrayOutputStream extends ByteArrayOutputStream {
    private static final int DEFAULT_SIZE = 1024;

    private final int initialBufferSize;

    public SpottyByteArrayOutputStream() {
        this(DEFAULT_SIZE);
    }

    public SpottyByteArrayOutputStream(int size) {
        super(size);

        this.initialBufferSize = size;
    }

    @Override
    public void write(byte @NotNull[] b) throws IOException {
        write(b, 0, b.length);
    }

    public synchronized void write(String text) throws IOException {
        write(text.getBytes());
    }

    public synchronized void capacity(int capacity) {
        if (capacity < 0) {
            throw new IndexOutOfBoundsException("capacity must be >= 0");
        }

        if (buf.length != capacity) {
            final byte[] buffer = new byte[capacity];
            count = Math.min(count, capacity);

            if (count > 0) {
                System.arraycopy(buf, 0, buffer, 0, count);
            }

            buf = buffer;
        }
    }

    public int capacity() {
        return buf.length;
    }

    @Override
    public synchronized void reset() {
        capacity(initialBufferSize);

        super.reset();
    }

    @Override
    public synchronized String toString() {
        if (count > 0) {
            return super.toString();
        }

        return "";
    }
}
