package spotty.server.handler;

import spotty.common.filter.Filter;
import spotty.common.json.Json;
import spotty.common.request.SpottyDefaultRequest;
import spotty.common.request.SpottyInnerRequest;
import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;
import spotty.server.router.SpottyRouter;
import spotty.server.router.route.RouteEntry;

import java.util.Collection;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static spotty.common.http.Headers.ACCEPT;

public final class DefaultRequestHandler implements RequestHandler {

    private final SpottyRouter router;

    public DefaultRequestHandler(SpottyRouter router) {
        this.router = notNull(router, "router");
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

        try {
            executeFilters(routeEntry.beforeFilters(), request, response);

            final Object res = routeEntry.route().handle(request, response);

            renderResult(response, res);
        } finally {
            executeFilters(routeEntry.afterFilters(), request, response);
        }
    }

    private void renderResult(SpottyResponse response, Object result) {
        if (result == null) {
            return;
        }

        if (result instanceof byte[]) {
            response.body((byte[]) result);
        } else if (APPLICATION_JSON.getMimeType().equals(response.contentType().getMimeType())) {
            response.body(Json.writeValueAsBytes(result));
        } else {
            response.body(result.toString());
        }
    }

    private void executeFilters(Collection<Filter> filters, SpottyRequest request, SpottyResponse response) throws Exception {
        for (Filter filter : filters) {
            filter.handle(request, response);
        }
    }
}
