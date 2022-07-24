package spotty.common.utils;

import spotty.common.stream.output.SpottyByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public final class IOUtils {
    private IOUtils() {
    }

    public static byte[] toByteArray(InputStream in) {
        try {
            final SpottyByteArrayOutputStream out = new SpottyByteArrayOutputStream();

            int read;
            final byte[] data = new byte[1024];
            while ((read = in.read(data)) > 0) {
                out.write(data, 0, read);
            }

            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String toString(InputStream in) throws UncheckedIOException {
        return new String(toByteArray(in));
    }

}
