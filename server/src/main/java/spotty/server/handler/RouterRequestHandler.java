package spotty.server.handler;

import lombok.SneakyThrows;
import spotty.common.json.Json;
import spotty.common.request.SpottyDefaultRequest;
import spotty.common.request.SpottyInnerRequest;
import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;
import spotty.server.router.Routable;
import spotty.server.router.route.RouteEntry;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public final class RouterRequestHandler implements RequestHandler {

    private final Routable routable;

    public RouterRequestHandler(Routable routable) {
        this.routable = notNull(routable, "routable");
    }

    @Override
    @SneakyThrows
    public void handle(SpottyInnerRequest innerRequest, SpottyResponse response) {
        final RouteEntry routeEntry = routable.getRoute(innerRequest.path(), innerRequest.method());
        innerRequest.pathParams(routeEntry.parsePathParams(innerRequest.path()));

        final SpottyRequest request = new SpottyDefaultRequest(innerRequest);
        final Object res = routeEntry.route.handle(request, response);

        renderResult(response, res);
    }

    private void renderResult(SpottyResponse response, Object result) {
        if (result == null) {
            return;
        }

        if (APPLICATION_JSON.getMimeType().equals(response.contentType().getMimeType())) {
            response.body(Json.writeValueAsBytes(result));
        } else if (result instanceof byte[]) {
            response.body((byte[]) result);
        } else {
            response.body(result.toString());
        }
    }
}
