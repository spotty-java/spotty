package spotty.common.utils;

import spotty.common.stream.output.SpottyByteArrayOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;

public final class IOUtils {

    public static byte[] toByteArray(URL url) {
        try (final InputStream in = url.openStream()) {
            return toByteArray(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] toByteArray(File file) {
        try (final InputStream in = new FileInputStream(file)) {
            return toByteArray(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] toByteArray(InputStream in) {
        try {
            final SpottyByteArrayOutputStream out = new SpottyByteArrayOutputStream();

            int read;
            final byte[] data = new byte[2048];
            while ((read = in.read(data)) > 0) {
                out.write(data, 0, read);
            }

            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String toString(InputStream in) {
        return new String(toByteArray(in));
    }

}
