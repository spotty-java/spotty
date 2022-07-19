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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.entity.ContentType.WILDCARD;
import static spotty.common.http.HttpStatus.NOT_FOUND;
import static spotty.server.router.RouteEntryCreator.normalizePath;

@VisibleForTesting
final class Routable {
    private static final String DEFAULT_ACCEPT_TYPE = WILDCARD.toString();

    // store handlers by link, so removing from it is also affected this list
    final SortedList sortedList = new SortedList();

    /*
    routing map
    {
        path: {
            httpMethod: {
                acceptType: routeEntry
            }
        }
    }
     */
    final Map<String, Map<HttpMethod, Map<String, RouteEntry>>> routes = new HashMap<>();

    void addRoute(String routePath, HttpMethod method, Route route) {
        addRoute(routePath, method, DEFAULT_ACCEPT_TYPE, route);
    }

    void addRoute(String routePath, HttpMethod method, String acceptType, Route route) {
        notNull(method, "method is null");
        notBlank(acceptType, "acceptType is empty");
        notNull(route, "route is null");

        final String path = notBlank(routePath, "path is empty").trim();
        final RouteEntry routeEntry = RouteEntryCreator.create(path, method, route);

        final Map<HttpMethod, Map<String, RouteEntry>> routeHandlers = routes.computeIfAbsent(
            routeEntry.pathNormalized(),
            pathNormalized -> {
                final Map<HttpMethod, Map<String, RouteEntry>> handlers = new HashMap<>();
                sortedList.add(new Value(pathNormalized, handlers, routeEntry.matcher()));

                return handlers;
            }
        );

        final Map<String, RouteEntry> routesWithAcceptType = routeHandlers.computeIfAbsent(method, __ -> new HashMap<>());
        if (routesWithAcceptType.containsKey(acceptType)) {
            throw new SpottyException("%s(%s) %s is exists already", method, acceptType, path);
        }

        routesWithAcceptType.put(acceptType, routeEntry);
    }

    void clearRoutes() {
        routes.clear();
        sortedList.clear();
    }

    boolean removeRoute(String routePath) {
        final String normalizedPath = normalizePath(routePath);
        return routes.remove(normalizedPath) != null && sortedList.removeByPath(normalizedPath);
    }

    boolean removeRoute(String routePath, HttpMethod method) {
        final Map<HttpMethod, Map<String, RouteEntry>> route = routes.get(normalizePath(routePath));
        if (route == null) {
            return false;
        }

        return route.remove(method) != null;
    }

    boolean removeRoute(String routePath, String acceptType, HttpMethod method) {
        final Map<HttpMethod, Map<String, RouteEntry>> route = routes.get(normalizePath(routePath));
        if (route == null) {
            return false;
        }

        final Map<String, RouteEntry> acceptTypeRoutes = route.get(method);
        if (acceptTypeRoutes == null) {
            return false;
        }

        return acceptTypeRoutes.remove(acceptType) != null;
    }

    @NotNull
    RouteEntry getRoute(String rawPath, HttpMethod method) throws SpottyHttpException {
        return getRoute(rawPath, null, method);
    }

    @NotNull
    RouteEntry getRoute(String rawPath, String acceptType, HttpMethod method) throws SpottyHttpException {
        Map<HttpMethod, Map<String, RouteEntry>> routes = this.routes.get(rawPath);
        if (routes == null) {
            routes = findMatch(rawPath);
        }

        final Map<String, RouteEntry> entry = routes.get(method);
        if (entry == null) {
            throw new SpottyHttpException(NOT_FOUND, format("route not found for %s %s", method, rawPath));
        }

        final String accept = defaultIfNull(acceptType, DEFAULT_ACCEPT_TYPE);
        RouteEntry routeEntry = entry.get(accept);
        if (routeEntry == null) {
            routeEntry = entry.get(DEFAULT_ACCEPT_TYPE);
        }

        if (routeEntry == null) {
            throw new SpottyHttpException(NOT_FOUND, format("route not found for %s(%s) %s", method, accept, rawPath));
        }

        return routeEntry;
    }

    private Map<HttpMethod, Map<String, RouteEntry>> findMatch(String rawPath) {
        for (int i = 0; i < sortedList.size(); i++) {
            final Value value = sortedList.get(i);
            if (value.matches(rawPath)) {
                return value.handlers;
            }
        }

        return emptyMap();
    }

    static class SortedList {
        private static final Comparator<Value> FROM_LONGEST_TO_SHORTEST_COMPARATOR =
            (a, b) -> b.pathNormalized.length() - a.pathNormalized.length();

        private final ArrayList<Value> values = new ArrayList<>();

        private void add(Value value) {
            values.add(value);
            values.sort(FROM_LONGEST_TO_SHORTEST_COMPARATOR);
        }

        private Value get(int index) {
            return values.get(index);
        }

        private boolean removeByPath(String pathNormalized) {
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i).pathNormalized.equals(pathNormalized)) {
                    return values.remove(i) != null;
                }
            }

            return false;
        }

        private void clear() {
            values.clear();
        }

        private int size() {
            return values.size();
        }

        void forEachRoute(Predicate<RouteEntry> predicate, Consumer<RouteEntry> consumer) {
            values.stream()
                .map(value -> value.handlers)
                .flatMap(map -> map.values().stream())
                .flatMap(map -> map.values().stream())
                .filter(predicate)
                .forEach(consumer);
        }

        @VisibleForTesting
        List<String> toNormalizedPaths() {
            return values.stream().map(v -> v.pathNormalized).collect(toList());
        }
    }

    static class Value {
        final String pathNormalized;
        final Map<HttpMethod, Map<String, RouteEntry>> handlers;
        final Pattern matcher;

        private Value(String pathNormalized, Map<HttpMethod, Map<String, RouteEntry>> handlers, Pattern matcher) {
            this.pathNormalized = pathNormalized;
            this.handlers = handlers;
            this.matcher = matcher;
        }

        boolean matches(String rawPath) {
            return matcher.matcher(rawPath).matches();
        }
    }
}
