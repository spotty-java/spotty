package spotty.common.stream.output;

import java.io.ByteArrayOutputStream;

public class SpottyByteArrayOutputStream extends ByteArrayOutputStream {
    private static final int DEFAULT_SIZE = 1024;

    private final int initialBufferSize;

    public SpottyByteArrayOutputStream() {
        this(DEFAULT_SIZE);
    }

    public SpottyByteArrayOutputStream(int size) {
        super(size);

        this.initialBufferSize = size;
    }

    public synchronized void write(String text) {
        writeBytes(text.getBytes());
    }

    public synchronized void capacity(int capacity) {
        if (capacity < 0) {
            throw new IndexOutOfBoundsException("capacity must be >= 0");
        }

        if (buf.length != capacity) {
            byte[] b = new byte[capacity];
            count = Math.min(count, capacity);

            if (count > 0) {
                System.arraycopy(buf, 0, b, 0, count);
            }

            buf = b;
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
