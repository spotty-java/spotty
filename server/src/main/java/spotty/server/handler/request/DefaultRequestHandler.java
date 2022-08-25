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
package spotty.server.handler.request;

import spotty.common.exception.SpottyException;
import spotty.common.filter.Filter;
import spotty.common.http.ContentEncoding;
import spotty.common.request.SpottyDefaultRequest;
import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;
import spotty.common.router.route.RouteEntry;
import spotty.server.compress.Compressor;
import spotty.server.router.SpottyRouter;
import spotty.server.session.SessionManager;

import java.util.Collection;

import static java.util.Arrays.asList;
import static spotty.common.http.HttpHeaders.ACCEPT;
import static spotty.common.http.HttpHeaders.CONTENT_ENCODING;
import static spotty.common.validation.Validation.notNull;

public final class DefaultRequestHandler implements RequestHandler {

    private final SpottyRouter router;
    private final Compressor compressor;
    private final SessionManager sessionManager;

    public DefaultRequestHandler(SpottyRouter router, Compressor compressor, SessionManager sessionManager) {
        this.router = notNull("router", router);
        this.compressor = notNull("compress", compressor);
        this.sessionManager = notNull("sessionManager", sessionManager);
    }

    @Override
    public void handle(SpottyDefaultRequest request, SpottyResponse response) throws Exception {
        final RouteEntry routeEntry = router.getRoute(
            request.path(),
            request.method(),
            request.headers().get(ACCEPT)
        );

        if (routeEntry.hasPathParamKeys()) {
            request.pathParamsObject(routeEntry.parsePathParams(request.path()));
        }

        // if session enabled, should register it for request
        sessionManager.register(request, response);

        final Object result;
        try {
            executeFilters(routeEntry.beforeFilters(), request, response);
            result = routeEntry.route().handle(request, response);
        } finally {
            executeFilters(routeEntry.afterFilters(), request, response);
        }

        byte[] body = response.body();
        if (result != null) {
            body = render().render(result);
        }

        if (body == null) {
            return;
        }

        if (response.headers().has(CONTENT_ENCODING)) {
            final ContentEncoding contentEncoding = ContentEncoding.of(response.headers().get(CONTENT_ENCODING));
            if (contentEncoding == null) {
                throw new SpottyException("Spotty supports " + asList(ContentEncoding.values()) + " compression");
            }

            body = compressor.compress(contentEncoding, body);
        }

        response.body(body);
    }

    private void executeFilters(Collection<Filter> filters, SpottyRequest request, SpottyResponse response) throws Exception {
        for (Filter filter : filters) {
            filter.handle(request, response);
        }
    }

}
