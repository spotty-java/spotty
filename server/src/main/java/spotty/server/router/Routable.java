package spotty.server.router;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import spotty.common.exception.SpottyException;
import spotty.common.exception.SpottyHttpException;
import spotty.common.http.HttpMethod;
import spotty.server.router.route.Route;
import spotty.server.router.route.RouteEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.Validate.notNull;
import static spotty.common.http.HttpStatus.NOT_FOUND;

@VisibleForTesting
final class Routable {
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

    @NotNull
    RouteEntry getRoute(String rawPath, HttpMethod method) throws SpottyHttpException {
        Map<HttpMethod, RouteEntry> routes = this.routes.get(rawPath);
        if (routes == null) {
            routes = findMatch(rawPath);
        }

        final RouteEntry entry = routes.get(method);
        if (entry == null) {
            throw new SpottyHttpException(NOT_FOUND, format("route not found for path %s and method %s", rawPath, method));
        }

        return entry;
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
