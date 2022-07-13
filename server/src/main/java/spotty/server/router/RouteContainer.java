package spotty.server.router;

import spotty.common.exception.SpottyException;
import spotty.common.http.HttpMethod;
import spotty.server.router.route.NotFoundRoute;
import spotty.server.router.route.Route;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.Validate.notNull;

final class RouteContainer {
    public static final Route NOT_FOUND_ROUTE = new NotFoundRoute();

    private final Map<String, Map<HttpMethod, Route>> routes = new HashMap<>();

    void addRoute(String routePath, HttpMethod method, Route route) {
        notNull(method, "method is null");
        notNull(route, "route is null");

        final String path = notNull(routePath, "path is null").trim();
        routes.compute(path, (__, routes) -> {
            Map<HttpMethod, Route> handlers = routes;
            if (handlers == null) {
                handlers = new HashMap<>();
            }

            if (handlers.containsKey(method)) {
                throw new SpottyException("path %s for method %s is ambitious", path, method);
            }

            handlers.put(method, route);
            return handlers;
        });
    }

    Route getRoute(String path, HttpMethod method) {
        final Map<HttpMethod, Route> routes = this.routes.getOrDefault(path, emptyMap());

        return routes.getOrDefault(method, NOT_FOUND_ROUTE);
    }
}
