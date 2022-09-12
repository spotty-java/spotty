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
package spotty.server.files.loader.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import spotty.common.response.SpottyResponse;
import spotty.common.utils.IOUtils;
import spotty.server.files.detector.TypeDetector;
import spotty.server.files.loader.FileLoader;

import java.net.URL;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.concurrent.TimeUnit.SECONDS;
import static spotty.common.http.HttpHeaders.CACHE_CONTROL;
import static spotty.common.http.HttpHeaders.EXPIRES;
import static spotty.common.validation.Validation.isNotNull;
import static spotty.common.validation.Validation.notNull;

public final class CacheFileLoader implements FileLoader {

    private final String cacheControl;
    private final LoadingCache<URL, Data> cache;

    public CacheFileLoader(TypeDetector typeDetector, long cacheTtl, long cacheSize) {
        notNull("typeDetector", typeDetector);

        cacheControl = "private, max-age=" + cacheTtl;
        cache = CacheBuilder.newBuilder()
            .maximumSize(cacheSize)
            .softValues()
            .expireAfterWrite(cacheTtl, SECONDS)
            .build(new CacheLoader<URL, Data>() {
                @Override
                public Data load(URL file) throws Exception {
                    final String mimeType = typeDetector.detect(file);
                    final String expires = RFC_1123_DATE_TIME.format(now(UTC).plusSeconds(cacheTtl));
                    final byte[] content = IOUtils.toByteArray(file);

                    return new Data(expires, mimeType, content);
                }
            });
    }

    @Override
    public byte[] loadFile(URL file, SpottyResponse response) throws Exception {
        notNull("file", file);
        notNull("response", response);

        try {
            final Data data = cache.get(file);

            response.headers().add(CACHE_CONTROL, cacheControl);
            response.headers().add(EXPIRES, data.expires);
            response.contentType(data.contentType);

            return data.content;
        } catch (Exception e) {
            if (isNotNull(e.getCause())) {
                throw (Exception) e.getCause();
            }

            throw e;
        }
    }

    private static class Data {
        final String expires;
        final String contentType;
        final byte[] content;

        private Data(String expires, String contentType, byte[] content) {
            this.expires = expires;
            this.contentType = contentType;
            this.content = content;
        }
    }

}
