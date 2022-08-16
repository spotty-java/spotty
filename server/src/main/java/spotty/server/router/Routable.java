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
package spotty.server.router;

import com.google.common.annotations.VisibleForTesting;
import spotty.common.exception.SpottyException;
import spotty.common.exception.SpottyHttpException;
import spotty.common.exception.SpottyNotFoundException;
import spotty.common.http.HttpMethod;
import spotty.common.router.route.Route;
import spotty.common.router.route.RouteEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static spotty.common.utils.RouterUtils.normalizePath;
import static spotty.common.validation.Validation.notBlank;
import static spotty.common.validation.Validation.notNull;
import static spotty.server.router.SpottyRouter.DEFAULT_ACCEPT_TYPE;

/**
 * Main Routing core class
 */
@VisibleForTesting
final class Routable {
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

    synchronized void addRoute(String routePath, HttpMethod method, Route route) {
        addRoute(routePath, method, DEFAULT_ACCEPT_TYPE, route);
    }

    synchronized void addRoute(String routePath, HttpMethod method, String acceptType, Route route) {
        notNull("method", method);
        notBlank("acceptType", acceptType);
        notNull("route", route);

        final String path = notBlank("path is empty", routePath).trim();
        final RouteEntry routeEntry = RouteEntryFactory.create(path, method, acceptType, route);

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

    synchronized void clearRoutes() {
        routes.clear();
        sortedList.clear();
    }

    synchronized boolean removeRoute(String routePath) {
        notBlank("routePath", routePath);

        final String normalizedPath = normalizePath(routePath);
        return routes.remove(normalizedPath) != null && sortedList.removeByPath(normalizedPath);
    }

    synchronized boolean removeRoute(String routePath, HttpMethod method) {
        notBlank("routePath", routePath);
        notNull("method", method);

        final Map<HttpMethod, Map<String, RouteEntry>> route = routes.get(normalizePath(routePath));
        if (route == null) {
            return false;
        }

        return route.remove(method) != null;
    }

    synchronized boolean removeRoute(String routePath, HttpMethod method, String acceptType) {
        notBlank("routePath", routePath);
        notNull("method", method);
        notBlank("acceptType", acceptType);

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

    RouteEntry getRoute(String rawPath, HttpMethod method) throws SpottyHttpException {
        return getRoute(rawPath, method, null);
    }

    RouteEntry getRoute(String rawPath, HttpMethod method, String acceptType) throws SpottyHttpException {
        Map<HttpMethod, Map<String, RouteEntry>> routes = this.routes.get(rawPath);
        if (routes == null) {
            routes = findMatch(rawPath);
        }

        final Map<String, RouteEntry> entry = routes.get(method);
        if (entry == null) {
            throw new SpottyNotFoundException("route not found for %s %s", method, rawPath);
        }

        final String accept = acceptType == null ? DEFAULT_ACCEPT_TYPE : acceptType;
        RouteEntry routeEntry = entry.get(accept);
        if (routeEntry == null) {
            routeEntry = entry.get(DEFAULT_ACCEPT_TYPE);
        }

        if (routeEntry == null) {
            throw new SpottyNotFoundException("route not found for %s(%s) %s", method, accept, rawPath);
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

    /**
     * Sort list by pathNormalized from longest to shortest to search matched route
     */
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

        void forEachRouteIf(Predicate<RouteEntry> predicate, Consumer<RouteEntry> consumer) {
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
