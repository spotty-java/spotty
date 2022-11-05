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

import java.io.InputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import static spotty.common.validation.Validation.notNull;

public final class Compressor {

    public InputStream compress(ContentEncoding encoding, InputStream body) throws Exception {
        notNull("encoding", encoding);
        notNull("body", body);

        switch (encoding) {
            case GZIP:
                body = new GZIPInputStream(body);
                break;
            case DEFLATE:
                body = new DeflaterInputStream(body);
                break;
            default:
                throw new SpottyException(encoding + " unsupported compression algorithm");
        }

        return body;
    }

}
