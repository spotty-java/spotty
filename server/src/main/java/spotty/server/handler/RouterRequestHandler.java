package spotty.server.handler;

import lombok.SneakyThrows;
import spotty.common.json.Json;
import spotty.common.request.SpottyDefaultRequest;
import spotty.common.request.SpottyInnerRequest;
import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;
import spotty.server.router.SpottyRouter;
import spotty.server.router.route.RouteEntry;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static spotty.common.http.Headers.ACCEPT;

public final class RouterRequestHandler implements RequestHandler {

    private final SpottyRouter router;

    public RouterRequestHandler(SpottyRouter router) {
        this.router = notNull(router, "router");
    }

    @Override
    @SneakyThrows
    public void handle(SpottyInnerRequest innerRequest, SpottyResponse response) {
        final RouteEntry routeEntry = router.getRoute(
            innerRequest.path(),
            innerRequest.headers().get(ACCEPT),
            innerRequest.method()
        );

        innerRequest.pathParams(routeEntry.parsePathParams(innerRequest.path()));

        final SpottyRequest request = new SpottyDefaultRequest(innerRequest);
        final Object res = routeEntry.route.handle(request, response);

        renderResult(response, res);
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
}
