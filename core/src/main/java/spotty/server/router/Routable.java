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
import spotty.common.exception.SpottyHttpException;
import spotty.common.exception.SpottyNotFoundException;
import spotty.common.exception.SpottyRouteDuplicationException;
import spotty.common.http.HttpMethod;
import spotty.common.router.route.Route;
import spotty.common.router.route.RouteEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static spotty.common.utils.RouterUtils.normalizePath;
import static spotty.common.validation.Validation.notBlank;
import static spotty.common.validation.Validation.notNull;
import static spotty.server.router.SpottyRouter.DEFAULT_ACCEPT_TYPE;

/**
 * Main routing core class responsible for managing routes and handling requests.
 * This class utilizes a Trie-based router for efficient route matching.
 */
@VisibleForTesting
final class Routable {
    private static final Function<?, Map<?, ?>> CREATE_NEW_MAP = __ -> new HashMap<>();

    // store handlers by link, so removing from it is also affected trie
    final TrieRoutes trieRoutes = new TrieRoutes();

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
    final Map<String, RouteNode> routes = new HashMap<>();

    /**
     * Adds a route to the routing table.
     *
     * @param routePath    The path of the route
     * @param method       The HTTP method of the route
     * @param route        The route to be added
     */
    synchronized void addRoute(String routePath, HttpMethod method, Route route) {
        addRoute(routePath, method, DEFAULT_ACCEPT_TYPE, route);
    }

    /**
     * Adds a route to the routing table.
     *
     * @param routePath    The path of the route
     * @param method       The HTTP method of the route
     * @param acceptType   The accept type of the route
     * @param route        The route to be added
     */
    synchronized void addRoute(String routePath, HttpMethod method, String acceptType, Route route) {
        notNull("method", method);
        notBlank("acceptType", acceptType);
        notNull("route", route);

        final String path = notBlank("path is empty", routePath).trim();
        final RouteEntry routeEntry = RouteEntryFactory.create(path, method, acceptType, route);

        final RouteNode routeNode = routes.computeIfAbsent(routeEntry.pathNormalized(), pathNormalized -> trieRoutes.add(pathNormalized, new HashMap<>()));
        final Map<String, RouteEntry> routesWithAcceptType = routeNode.handlers.computeIfAbsent(method, createEmptyMap());
        if (routesWithAcceptType.containsKey(acceptType)) {
            throw new SpottyRouteDuplicationException("%s(%s) %s is exists already", method, acceptType, path);
        }

        routesWithAcceptType.put(acceptType, routeEntry);
    }

    /**
     * Clears all registered routes from the routing table.
     */
    synchronized void clearRoutes() {
        routes.clear();
        trieRoutes.clear();
    }

    /**
     * Removes a route from the routing table by its path.
     *
     * @param routePath    The path of the route to be removed
     * @return             True if the route was successfully removed, false otherwise
     */
    synchronized boolean removeRoute(String routePath) {
        notBlank("routePath", routePath);

        final String normalizedPath = normalizePath(routePath);
        return routes.remove(normalizedPath) != null && trieRoutes.removeExactly(normalizedPath);
    }

    /**
     * Removes a route from the routing table by its path and HTTP method.
     *
     * @param routePath    The path of the route to be removed
     * @param method       The HTTP method of the route to be removed
     * @return             True if the route was successfully removed, false otherwise
     */
    synchronized boolean removeRoute(String routePath, HttpMethod method) {
        notBlank("routePath", routePath);
        notNull("method", method);

        final RouteNode node = routes.get(normalizePath(routePath));
        if (node == null) {
            return false;
        }

        // remove method from handlers, this is not require update trieRoutes as handlers will be removed by link
        return node.handlers.remove(method) != null;
    }

    /**
     * Removes a route from the routing table by its path, HTTP method, and accept type.
     *
     * @param routePath    The path of the route to be removed
     * @param method       The HTTP method of the route to be removed
     * @param acceptType   The accept type of the route to be removed
     * @return             True if the route was successfully removed, false otherwise
     */
    synchronized boolean removeRoute(String routePath, HttpMethod method, String acceptType) {
        notBlank("routePath", routePath);
        notNull("method", method);
        notBlank("acceptType", acceptType);

        final RouteNode node = routes.get(normalizePath(routePath));
        if (node == null) {
            return false;
        }

        final Map<String, RouteEntry> acceptTypeRoutes = node.handlers.get(method);
        if (acceptTypeRoutes == null) {
            return false;
        }

        return acceptTypeRoutes.remove(acceptType) != null;
    }

    /**
     * Retrieves the route entry for a given raw path and HTTP method.
     *
     * @param rawPath      The raw path of the request
     * @param method       The HTTP method of the request
     * @return             The route entry matching the given path and method
     * @throws SpottyHttpException if the route is not found
     */
    RouteEntry getRoute(String rawPath, HttpMethod method) throws SpottyHttpException {
        return getRoute(rawPath, method, null);
    }

    /**
     * Retrieves the route entry for a given raw path, HTTP method, and accept type.
     *
     * @param rawPath      The raw path of the request
     * @param method       The HTTP method of the request
     * @param acceptType   The accept type of the request
     * @return             The route entry matching the given path, method, and accept type
     * @throws SpottyHttpException if the route is not found
     */
    RouteEntry getRoute(String rawPath, HttpMethod method, String acceptType) throws SpottyHttpException {
        RouteNode routeNode = this.routes.get(rawPath);
        if (routeNode == null) {
            routeNode = trieRoutes.findRouteNode(rawPath);
        }

        if (routeNode == null) {
            throw new SpottyNotFoundException("route not found for %s", rawPath);
        }

        final Map<String, RouteEntry> entry = routeNode.handlers.get(method);
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

    @SuppressWarnings("unchecked")
    private static <P, K, V> Function<P, Map<K, V>> createEmptyMap() {
        return (Function<P, Map<K, V>>) (Function<?, ?>) CREATE_NEW_MAP;
    }
}
