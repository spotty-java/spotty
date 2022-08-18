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
package spotty.common.utils;

import spotty.common.stream.output.SpottyByteArrayOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.ByteBuffer;

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
        try (final SpottyByteArrayOutputStream out = new SpottyByteArrayOutputStream()) {
            int read;
            final byte[] data = new byte[2048];
            while ((read = in.read(data)) != -1) {
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

    /**
     * copy src buffer to dest buffer by remaining of both
     *
     * @param src  buffer
     * @param dest buffer
     * @return count read bytes
     */
    public static int bufferCopyRemaining(ByteBuffer src, ByteBuffer dest) {
        final int init = dest.position();
        while (src.hasRemaining() && dest.hasRemaining()) {
            dest.put(src.get());
        }

        return dest.position() - init;
    }

}
