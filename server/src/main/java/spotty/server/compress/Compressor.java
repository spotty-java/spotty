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
package spotty.server.compress;

import spotty.common.exception.SpottyException;
import spotty.common.http.ContentEncoding;
import spotty.common.stream.output.SpottyByteArrayOutputStream;

import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static spotty.common.validation.Validation.notNull;

public final class Compressor {

    public byte[] compress(ContentEncoding encoding, byte[] body) throws Exception {
        notNull("encoding", encoding);

        final SpottyByteArrayOutputStream out = new SpottyByteArrayOutputStream(body.length);
        final OutputStream compressor;
        switch (encoding) {
            case GZIP:
                compressor = new GZIPOutputStream(out);
                break;
            case DEFLATE:
                compressor = new DeflaterOutputStream(out);
                break;
            default:
                throw new SpottyException(encoding + " unsupported compression algorithm");
        }

        try {
            compressor.write(body);
        } finally {
            compressor.close();
        }

        return out.toByteArray();
    }

}
