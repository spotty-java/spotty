package spotty.server.handler.request;

import spotty.common.exception.SpottyException;
import spotty.common.filter.Filter;
import spotty.common.http.ContentEncoding;
import spotty.common.request.DefaultSpottyRequest;
import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;
import spotty.server.compress.Compressor;
import spotty.server.router.SpottyRouter;
import spotty.server.router.route.RouteEntry;

import java.util.Arrays;
import java.util.Collection;

import static spotty.common.http.HttpHeaders.ACCEPT;
import static spotty.common.http.HttpHeaders.CONTENT_ENCODING;
import static spotty.common.validation.Validation.notNull;

public final class DefaultRequestHandler implements RequestHandler {

    private final SpottyRouter router;
    private final Compressor compressor;

    public DefaultRequestHandler(SpottyRouter router, Compressor compressor) {
        this.router = notNull("router", router);
        this.compressor = notNull("compress", compressor);
    }

    @Override
    public void handle(DefaultSpottyRequest request, SpottyResponse response) throws Exception {
        final RouteEntry routeEntry = router.getRoute(
            request.path(),
            request.method(),
            request.headers().get(ACCEPT)
        );

        if (routeEntry.hasPathParamKeys()) {
            request.pathParams(routeEntry.parsePathParams(request.path()));
        }

        final Object result;
        try {
            executeFilters(routeEntry.beforeFilters(), request, response);
            result = routeEntry.route().handle(request, response);
        } finally {
            executeFilters(routeEntry.afterFilters(), request, response);
        }

        if (result == null) {
            return;
        }

        byte[] body = render().render(result);
        if (response.headers().has(CONTENT_ENCODING)) {
            final ContentEncoding contentEncoding = ContentEncoding.of(response.headers().get(CONTENT_ENCODING));
            if (contentEncoding == null) {
                throw new SpottyException("Spotty supports " + Arrays.asList(ContentEncoding.values()) + " compression");
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
