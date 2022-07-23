package spotty.common.stream.input;

import spotty.common.stream.output.SpottyByteArrayOutputStream;
import spotty.common.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.Math.min;

public final class SpottyInputStream extends BufferedInputStream {
    private final SpottyByteArrayOutputStream LINE = new SpottyByteArrayOutputStream(256);

    private long read = 0;
    private long limitBytes = Long.MAX_VALUE;

    public SpottyInputStream(InputStream in) {
        super(in, 1024);
    }

    private SpottyInputStream(InputStream in, int bufferSize) {
        super(in, bufferSize);
    }

    public synchronized void fixedContentSize(long limitBytes) {
        this.limitBytes = read + limitBytes;
    }

    @Override
    public synchronized int read() throws IOException {
        if (read >= limitBytes) {
            return -1;
        }

        final int b = super.read();
        read++;

        return b;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int toRead = (int) min(len, limitBytes - read);
        if (toRead <= 0) {
            return -1;
        }

        final int read = super.read(b, off, toRead);
        this.read += read;

        return read;
    }

    /**
     * Reads a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), a carriage return
     * followed immediately by a line feed, or by reaching the end-of-file
     * (EOF).
     *
     * @return A String containing the contents of the line, not including
     * any line-termination characters, or null if the end of the
     * stream has been reached without reading any characters
     * @throws IOException If an I/O error occurs
     */
    public synchronized String readLine() throws IOException {
        try {
            boolean endOfLine = false;

            int read;
            while ((read = read()) >= 0) {
                byte b = (byte) read;
                if (b == '\r') {
                    continue;
                }

                if (b == '\n') {
                    endOfLine = true;
                    break;
                }

                LINE.write(b);
            }

            if (!endOfLine && LINE.size() == 0)
                return null;

            return LINE.size() == 0 ? "" : LINE.toString();
        } finally {
            LINE.reset();
        }
    }

    public byte[] readAllBytes() {
        return IOUtils.toByteArray(this);
    }

}
