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
package spotty.common.request;

import spotty.common.http.HttpHeaders;
import spotty.common.http.HttpMethod;
import spotty.common.session.Session;

import java.util.Map;
import java.util.Set;

public interface SpottyRequest {
    String protocol();

    String scheme();

    HttpMethod method();

    String path();

    int contentLength();

    String contentType();

    String host();

    String ip();

    int port();

    Map<String, String> cookies();

    HttpHeaders headers();

    Map<String, String> params();

    String param(String name);

    Map<String, Set<String>> queryParamsMap();

    Set<String> queryParams();

    Set<String> queryParams(String name);

    String queryParam(String name);

    void attach(Object attachment);

    Object attachment();

    Session session();

    byte[] body();

}
