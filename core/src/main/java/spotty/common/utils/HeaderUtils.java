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

import spotty.common.exception.SpottyException;
import spotty.common.exception.SpottyHttpException;
import spotty.common.http.HttpMethod;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static spotty.common.http.HttpHeaders.CONTENT_LENGTH;
import static spotty.common.http.HttpStatus.BAD_REQUEST;

public final class HeaderUtils {

    public static int parseContentLength(String contentLength) {
        try {
            return Integer.parseInt(contentLength);
        } catch (NumberFormatException e) {
            throw new SpottyHttpException(BAD_REQUEST, "invalid " + CONTENT_LENGTH);
        }
    }

    public static HttpMethod parseHttpMethod(String method) {
        final HttpMethod res = HttpMethod.resolve(method.toUpperCase());
        if (res == null) {
            throw new SpottyHttpException(BAD_REQUEST, "unsupported method " + method);
        }

        return res;
    }

    public static URI parseUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new SpottyException("invalid uri syntax %s", e, uri);
        }
    }

    public static Map<String, String> parseCookies(String cookiesString) {
        final Map<String, String> cookies = new HashMap<>();
        for (String cookie : cookiesString.split(";")) {
            final String[] parts = cookie.split("=");
            final String name = parts[0].trim();
            final String value = parts.length == 2 ? parts[1].trim() : "";

            cookies.put(name, value);
        }

        return unmodifiableMap(cookies);
    }
}
