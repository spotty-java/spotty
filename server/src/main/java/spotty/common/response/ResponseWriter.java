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
package spotty.common.response;

import spotty.common.stream.output.SpottyByteArrayOutputStream;

import static spotty.common.http.HttpHeaders.CONTENT_LENGTH;
import static spotty.common.http.HttpHeaders.CONTENT_TYPE;
import static spotty.common.http.HttpHeaders.SET_COOKIE;

/**
 * Single thread use only
 */
public final class ResponseWriter {
    private static final String HEADER_SPLITTER = ": ";

    private final SpottyByteArrayOutputStream writer = new SpottyByteArrayOutputStream(2048);

    public byte[] write(SpottyResponse response) {
        try {
            writer.print(response.protocol()); writer.print(" "); writer.print(response.status().toString());
            writer.println();

            writer.print(CONTENT_LENGTH); writer.print(HEADER_SPLITTER); writer.print(Integer.toString(response.contentLength()));
            writer.println();

            if (response.contentType() != null) {
                writer.print(CONTENT_TYPE); writer.print(HEADER_SPLITTER); writer.print(response.contentType());
                writer.println();
            }

            response.headers()
                .forEach((name, value) -> {
                    writer.print(name); writer.print(HEADER_SPLITTER); writer.print(value);
                    writer.println();
                });

            response.cookies()
                .forEach(cookie -> {
                    writer.print(SET_COOKIE); writer.print(HEADER_SPLITTER); writer.print(cookie.toString());
                    writer.println();
                });

            writer.println();

            if (response.body() != null) {
                writer.write(response.body());
            }

            return writer.toByteArray();
        } finally {
            writer.reset();
        }
    }

}
