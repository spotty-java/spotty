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
package spotty.common.http;

import spotty.common.exception.SpottyException;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;

public enum ContentEncoding {
    GZIP, DEFLATE;

    private static final Map<String, ContentEncoding> MAPPING = new HashMap<>();

    static {
        MAPPING.put("gzip", GZIP);
        MAPPING.put("GZIP", GZIP);
        MAPPING.put("deflate", DEFLATE);
        MAPPING.put("DEFLATE", DEFLATE);
    }

    public static ContentEncoding of(String name) {
        final ContentEncoding contentEncoding = MAPPING.get(name);
        if (contentEncoding == null) {
            throw new SpottyException("Spotty supports " + asList(ContentEncoding.values()) + " compression");
        }

        return contentEncoding;
    }
}
