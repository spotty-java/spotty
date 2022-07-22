package spotty.server.handler.request;

import spotty.common.exception.SpottyException;
import spotty.common.filter.Filter;
import spotty.common.http.ContentEncoding;
import spotty.common.request.SpottyDefaultRequest;
import spotty.common.request.SpottyInnerRequest;
import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;
import spotty.server.compress.Compressor;
import spotty.server.render.ResponseRender;
import spotty.server.router.SpottyRouter;
import spotty.server.router.route.RouteEntry;

import java.util.Arrays;
import java.util.Collection;

import static org.apache.commons.lang3.Validate.notNull;
import static spotty.common.http.HttpHeaders.ACCEPT;
import static spotty.common.http.HttpHeaders.CONTENT_ENCODING;

public final class DefaultRequestHandler implements RequestHandler {

    private final SpottyRouter router;
    private final ResponseRender render;
    private final Compressor compressor;

    public DefaultRequestHandler(SpottyRouter router, ResponseRender render, Compressor compressor) {
        this.router = notNull(router, "router");
        this.render = notNull(render, "render");
        this.compressor = notNull(compressor, "compress");
    }

    @Override
    public void handle(SpottyInnerRequest innerRequest, SpottyResponse response) throws Exception {
        final RouteEntry routeEntry = router.getRoute(
            innerRequest.path(),
            innerRequest.method(),
            innerRequest.headers().get(ACCEPT)
        );

        innerRequest.pathParams(routeEntry.parsePathParams(innerRequest.path()));

        final SpottyRequest request = new SpottyDefaultRequest(innerRequest);

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

        byte[] body = render.render(response, result);
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
