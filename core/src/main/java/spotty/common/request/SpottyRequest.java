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
import spotty.common.http.HttpProtocol;
import spotty.common.session.Session;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public interface SpottyRequest {
    /**
     * @return request protocol
     */
    HttpProtocol protocol();

    /**
     * @return request scheme (http)
     */
    String scheme();

    /**
     * @return request HTTP method
     */
    HttpMethod method();

    /**
     * @return request path
     */
    String path();

    /**
     * @return request content-length
     */
    int contentLength();

    /**
     * @return request content-type
     */
    String contentType();

    /**
     * @return host of client
     */
    String host();

    /**
     * @return ip of client
     */
    String ip();

    /**
     * @return port of client
     */
    int port();

    /**
     * @return map of all request cookies
     */
    Map<String, String> cookies();

    /**
     * @return request http headers
     */
    HttpHeaders headers();

    /**
     * map containing all route params
     *
     * @return request path params
     */
    Map<String, String> pathParams();

    /**
     * Returns the value of the provided route pattern parameter.
     * Example: parameter 'name' from the following pattern: (get '/hello/:name')
     *
     * @param name param name
     * @return null if the given param is null or not found
     */
    String pathParam(String name);

    /**
     * @return all request query parameters
     */
    Map<String, Set<String>> queryParamsMap();

    /**
     * @return all query param names
     */
    Set<String> queryParams();

    /**
     * @param name query param name
     * @return query param values
     */
    Set<String> queryParams(String name);

    /**
     * @param name query param name
     * @return query param value
     */
    String queryParam(String name);

    /**
     * set an attachment object (can be fetched in filters/routes later in the chain)
     * @param attachment attachment object
     */
    void attach(Object attachment);

    /**
     * @return attachment object
     */
    Object attachment();

    /**
     * Returns the current session associated with this request, or null if session disabled
     *
     * @return the session associated with this request or <code>null</code> if session disabled
     */
    Session session();

    /**
     * @return request body as bytes
     */
    byte[] body();

    /**
     * @return request body as string
     */
    String bodyAsString();

    InputStream bodyAsStream();

}
