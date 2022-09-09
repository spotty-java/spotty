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
import spotty.common.exception.SpottyValidationException;
import spotty.common.filter.Filter;
import spotty.common.http.HttpMethod;
import spotty.common.router.route.Route;
import spotty.common.router.route.RouteEntry;
import spotty.common.router.route.RouteGroup;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.asList;
import static spotty.common.http.HttpMethod.CONNECT;
import static spotty.common.http.HttpMethod.DELETE;
import static spotty.common.http.HttpMethod.GET;
import static spotty.common.http.HttpMethod.HEAD;
import static spotty.common.http.HttpMethod.OPTIONS;
import static spotty.common.http.HttpMethod.PATCH;
import static spotty.common.http.HttpMethod.POST;
import static spotty.common.http.HttpMethod.PUT;
import static spotty.common.http.HttpMethod.TRACE;
import static spotty.common.utils.RouterUtils.compileMatcher;

/**
 * Main facade for routing, add, remove routes and filters
 */
public final class SpottyRouter {
    public static final String DEFAULT_ACCEPT_TYPE = "*/*";

    // queue to build prefix for path
    private final Deque<String> pathPrefixStack = new LinkedList<>();
    private final Routable routable = new Routable();

    private final List<FilterContainer> beforeFilters = new ArrayList<>();
    private final List<FilterContainer> afterFilters = new ArrayList<>();

    /**
     * Add a path-prefix to the routes declared in the routeGroup
     * The path() method adds a path-fragment to a path-stack, adds
     * routes from the routeGroup, then pops the path-fragment again.
     * It's used for separating routes into groups, for example:
     *
     * <pre>
     * {@code
     *     path("/api/user", () -> {
     *          post("/add",   User::add);
     *          put("/change", User::change);
     *     });
     * }
     * </pre>
     * <p>
     * Multiple path() calls can be nested.
     *
     * @param pathTemplate the path to prefix routes with
     * @param group        group of routes (can also contain path() calls)
     */
    public void path(String pathTemplate, RouteGroup group) {
        pathPrefixStack.addLast(pathTemplate);
        group.addRoutes();
        pathPrefixStack.removeLast();
    }

    /**
     * Maps an array of filters to be executed before any routes
     *
     * @param filter the filter
     */
    public void before(Filter filter) {
        before("*", filter);
    }

    /**
     * Maps an array of filters to be executed after any routes
     *
     * @param filter the filter
     */
    public void after(Filter filter) {
        after("*", filter);
    }

    /**
     * Maps an array of filters to be executed before any matching routes by path
     *
     * @param pathTemplate the route path
     * @param filter       the filter
     */
    public void before(String pathTemplate, Filter filter) {
        before(pathTemplate, null, filter);
    }

    /**
     * Maps an array of filters to be executed after any matching routes by path
     *
     * @param pathTemplate the route path
     * @param filter       the filter
     */
    public void after(String pathTemplate, Filter filter) {
        after(pathTemplate, null, filter);
    }

    /**
     * Maps an array of filters to be executed before any matching routes by path and http method
     *
     * @param pathTemplate the route path
     * @param method       the route HTTP METHOD
     * @param filter       the filter
     */
    public void before(String pathTemplate, HttpMethod method, Filter filter) {
        before(pathTemplate, method, null, filter);
    }

    /**
     * Maps an array of filters to be executed after any matching routes by path and http method
     *
     * @param pathTemplate the route path
     * @param method       the route HTTP METHOD
     * @param filter       the filter
     */
    public void after(String pathTemplate, HttpMethod method, Filter filter) {
        after(pathTemplate, method, null, filter);
    }

    /**
     * Maps an array of filters to be executed before any matching routes by path, http method and accept-type
     *
     * @param pathTemplate the route path
     * @param method       the route HTTP METHOD
     * @param acceptType   the route Accept-Type
     * @param filter       the filter
     */
    public void before(String pathTemplate, HttpMethod method, String acceptType, Filter filter) {
        final Pattern matcher = compileMatcher(pathWithPrefix(pathTemplate)).matcher;

        addFilterToRoute(
            matcher,
            method,
            acceptType,
            filter,
            RouteEntry::addBeforeFilter
        );

        beforeFilters.add(new FilterContainer(matcher, method, acceptType, filter));
    }

    /**
     * Maps an array of filters to be executed after any matching routes by path, http method and accept-type
     *
     * @param pathTemplate the route path
     * @param method       the route HTTP METHOD
     * @param acceptType   the route Accept-Type
     * @param filter       the filter
     */
    public void after(String pathTemplate, HttpMethod method, String acceptType, Filter filter) {
        final Pattern matcher = compileMatcher(pathWithPrefix(pathTemplate)).matcher;

        addFilterToRoute(
            matcher,
            method,
            acceptType,
            filter,
            RouteEntry::addAfterFilter
        );

        afterFilters.add(new FilterContainer(matcher, method, acceptType, filter));
    }

    /**
     * Map the route for HTTP GET requests
     *
     * @param pathTemplate the route path
     * @param route        the route handler
     */
    public void get(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), GET, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP POST requests
     *
     * @param pathTemplate the route path
     * @param route        the route handler
     */
    public void post(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), POST, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP PUT requests
     *
     * @param pathTemplate the route path
     * @param route        the route handler
     */
    public void put(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), PUT, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP PATCH requests
     *
     * @param pathTemplate the route path
     * @param route        the route handler
     */
    public void patch(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), PATCH, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP DELETE requests
     *
     * @param pathTemplate the route path
     * @param route        the route handler
     */
    public void delete(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), DELETE, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP HEAD requests
     *
     * @param pathTemplate the route path
     * @param route        the route handler
     */
    public void head(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), HEAD, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP TRACE requests
     *
     * @param pathTemplate the route path
     * @param route        the route handler
     */
    public void trace(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), TRACE, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP CONNECT requests
     *
     * @param pathTemplate the route path
     * @param route        the route handler
     */
    public void connect(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), CONNECT, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP OPTIONS requests
     *
     * @param pathTemplate the route path
     * @param route        the route handler
     */
    public void options(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), OPTIONS, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP GET requests
     *
     * @param pathTemplate the route path
     * @param acceptType   the accept-type that route bind to
     * @param route        the route handler
     */
    public void get(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), GET, acceptType, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP POST requests
     *
     * @param pathTemplate the route path
     * @param acceptType   the accept-type that route bind to
     * @param route        the route handler
     */
    public void post(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), POST, acceptType, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP PUT requests
     *
     * @param pathTemplate the route path
     * @param acceptType   the accept-type that route bind to
     * @param route        the route handler
     */
    public void put(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), PUT, acceptType, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP PATCH requests
     *
     * @param pathTemplate the route path
     * @param acceptType   the accept-type that route bind to
     * @param route        the route handler
     */
    public void patch(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), PATCH, acceptType, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP DELETE requests
     *
     * @param pathTemplate the route path
     * @param acceptType   the accept-type that route bind to
     * @param route        the route handler
     */
    public void delete(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), DELETE, acceptType, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP HEAD requests
     *
     * @param pathTemplate the route path
     * @param acceptType   the accept-type that route bind to
     * @param route        the route handler
     */
    public void head(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), HEAD, acceptType, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP TRACE requests
     *
     * @param pathTemplate the route path
     * @param acceptType   the accept-type that route bind to
     * @param route        the route handler
     */
    public void trace(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), TRACE, acceptType, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP CONNECT requests
     *
     * @param pathTemplate the route path
     * @param acceptType   the accept-type that route bind to
     * @param route        the route handler
     */
    public void connect(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), CONNECT, acceptType, route);
        registerAllMatchedFilters();
    }

    /**
     * Map the route for HTTP OPTIONS requests
     *
     * @param pathTemplate the route path
     * @param acceptType   the accept-type that route bind to
     * @param route        the route handler
     */
    public void options(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), OPTIONS, acceptType, route);
        registerAllMatchedFilters();
    }

    /**
     * Find route by path and http method
     *
     * @param rawPath route path
     * @param method  route http method
     * @return RouteEntry
     * @throws SpottyHttpException if route not found
     */
    public RouteEntry getRoute(String rawPath, HttpMethod method) throws SpottyHttpException {
        return routable.getRoute(rawPath, method);
    }

    /**
     * Find route by path, http method and accept-type
     *
     * @param rawPath    route path
     * @param method     route http method
     * @param acceptType route accept-type
     * @return RouteEntry
     * @throws SpottyHttpException if route not found
     */
    public RouteEntry getRoute(String rawPath, HttpMethod method, String acceptType) throws SpottyHttpException {
        return routable.getRoute(rawPath, method, acceptType);
    }

    /**
     * clear all routes
     */
    public void clearRoutes() {
        routable.clearRoutes();
    }

    /**
     * Remove a particular route from the collection of those that have been previously routed.
     * Search for previously established routes using the given path and unmaps any matches that are found.
     *
     * @param pathTemplate the route path
     * @return true if this is a matching route which has been previously routed
     * @throws SpottyValidationException if pathTemplate is null or blank
     */
    public boolean removeRoute(String pathTemplate) throws SpottyValidationException {
        return routable.removeRoute(pathTemplate);
    }

    /**
     * Remove a particular route from the collection of those that have been previously routed.
     * Search for previously established routes using the given path and HTTP method, unmaps any
     * matches that are found.
     *
     * @param pathTemplate the route path
     * @param method       the route HTTP METHOD
     * @return true if this is a matching route which has been previously routed
     * @throws SpottyValidationException if pathTemplate or method is null or blank
     */
    public boolean removeRoute(String pathTemplate, HttpMethod method) throws SpottyValidationException {
        return routable.removeRoute(pathTemplate, method);
    }

    /**
     * Remove a particular route from the collection of those that have been previously routed.
     * Search for previously established routes using the given path, acceptType and HTTP method, unmaps any
     * matches that are found.
     *
     * @param pathTemplate the route path
     * @param acceptType   the route accept-type
     * @param method       the route HTTP METHOD
     * @return true if this is a matching route which has been previously routed
     * @throws SpottyValidationException if pathTemplate, acceptType or method is null or blank
     */
    public boolean removeRoute(String pathTemplate, String acceptType, HttpMethod method) throws SpottyValidationException {
        return routable.removeRoute(pathTemplate, method, acceptType);
    }

    // register filters after route added,
    private void registerAllMatchedFilters() {
        beforeFilters.forEach(container -> {
            addFilterToRoute(
                container.matcher,
                container.method,
                container.acceptType,
                container.filter,
                RouteEntry::addBeforeFilter
            );
        });

        afterFilters.forEach(container -> {
            addFilterToRoute(
                container.matcher,
                container.method,
                container.acceptType,
                container.filter,
                RouteEntry::addAfterFilter
            );
        });
    }

    private void addFilterToRoute(Pattern matcher, HttpMethod method, String acceptType, Filter filter, BiConsumer<RouteEntry, Filter> adder) {
        routable.sortedList.forEachRouteIf(
            route -> {
                if (!matcher.matcher(route.pathNormalized()).matches()) {
                    return false;
                }

                if (method != null && method != route.httpMethod()) {
                    return false;
                }

                if (acceptType == null) {
                    return true;
                }

                return acceptType.equals(route.acceptType());
            },
            route -> adder.accept(route, filter)
        );
    }

    @VisibleForTesting
    String pathWithPrefix(String pathTemplate) {
        if (pathPrefixStack.isEmpty()) {
            return pathTemplate;
        }

        return String.join("", pathPrefixStack) + pathTemplate;
    }

    private static class FilterContainer {
        final Pattern matcher;
        final HttpMethod method;
        final String acceptType;
        final Filter filter;

        private FilterContainer(Pattern matcher, HttpMethod method, String acceptType, Filter filter) {
            this.matcher = matcher;
            this.method = method;
            this.acceptType = acceptType;
            this.filter = filter;
        }
    }

}
