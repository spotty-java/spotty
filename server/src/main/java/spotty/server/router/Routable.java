package spotty.server.router;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import spotty.common.exception.SpottyException;
import spotty.common.http.HttpMethod;
import spotty.server.router.route.NotFoundRoute;
import spotty.server.router.route.Route;
import spotty.server.router.route.RouteEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.Validate.notNull;

@VisibleForTesting
final class Routable {
    public static final Route NOT_FOUND_ROUTE = new NotFoundRoute();

    final SortedList sortedList = new SortedList();
    final Map<String, Map<HttpMethod, RouteEntry>> routes = new HashMap<>();

    void addRoute(String routePath, HttpMethod method, Route route) {
        notNull(method, "method is null");
        notNull(route, "route is null");

        final String path = notNull(routePath, "path is null").trim();
        final RouteEntry routeEntry = RouteEntryCreator.create(path, method, route);

        final Map<HttpMethod, RouteEntry> routeHandlers = routes.computeIfAbsent(
            routeEntry.pathNormalized,
            pathNormalized -> {
                final Map<HttpMethod, RouteEntry> handlers = new HashMap<>();
                sortedList.add(new Value(pathNormalized, handlers, routeEntry.matcher));

                return handlers;
            }
        );

        if (routeHandlers.containsKey(method)) {
            throw new SpottyException("path %s for method %s is exists already", path, method);
        }

        routeHandlers.put(method, routeEntry);
    }

    @Nullable
    RouteEntry getRoute(String rawPath, HttpMethod method) {
        Map<HttpMethod, RouteEntry> routes = this.routes.get(rawPath);
        if (routes == null) {
            routes = findMatch(rawPath);
        }

        return routes.get(method);
    }

    private Map<HttpMethod, RouteEntry> findMatch(String rawPath) {
        for (int i = 0; i < sortedList.size(); i++) {
            final Value value = sortedList.get(i);
            if (value.matches(rawPath)) {
                return value.handlers;
            }
        }

        return emptyMap();
    }

    private static class SortedList {
        private static final Comparator<Value> FROM_LONGEST_TO_SHORTEST_COMPARATOR =
            (a, b) -> b.pathNormalized.length() - a.pathNormalized.length();

        final ArrayList<Value> values = new ArrayList<>();

        void add(Value value) {
            values.add(value);
            values.sort(FROM_LONGEST_TO_SHORTEST_COMPARATOR);
        }

        Value get(int index) {
            return values.get(index);
        }

        int size() {
            return values.size();
        }

        List<String> toNormalizedPaths() {
            return values.stream().map(v -> v.pathNormalized).collect(toList());
        }
    }

    private static class Value {
        final String pathNormalized;
        final Map<HttpMethod, RouteEntry> handlers;
        final Pattern matcher;

        private Value(String pathNormalized, Map<HttpMethod, RouteEntry> handlers, Pattern matcher) {
            this.pathNormalized = pathNormalized;
            this.handlers = handlers;
            this.matcher = matcher;
        }

        boolean matches(String rawPath) {
            return matcher.matcher(rawPath).matches();
        }
    }
}
